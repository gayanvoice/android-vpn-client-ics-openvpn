/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package de.blinkt.openvpn.core;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.buzz.vpn.BuildConfig;
import com.buzz.vpn.Data;
import com.buzz.vpn.MainActivity;
import com.buzz.vpn.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.VpnStatus.ByteCountListener;
import de.blinkt.openvpn.core.VpnStatus.StateListener;

import static com.buzz.vpn.Data.LongDataUsage;
import static de.blinkt.openvpn.core.ConnectionStatus.LEVEL_CONNECTED;
import static de.blinkt.openvpn.core.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT;
import static de.blinkt.openvpn.core.NetworkSpace.ipAddress;

public class OpenVPNService extends VpnService implements StateListener, Callback, ByteCountListener, IOpenVPNServiceInternal {
    public static final String START_SERVICE = "de.blinkt.openvpn.START_SERVICE";
    public static final String START_SERVICE_STICKY = "de.blinkt.openvpn.START_SERVICE_STICKY";
    public static final String ALWAYS_SHOW_NOTIFICATION = "de.blinkt.openvpn.NOTIFICATION_ALWAYS_VISIBLE";
    public static final String DISCONNECT_VPN = "de.blinkt.openvpn.DISCONNECT_VPN";
    public static final String NOTIFICATION_CHANNEL_BG_ID = "vpn_bg";
    public static final String NOTIFICATION_CHANNEL_NEWSTATUS_ID = "openvpn_newstat";
    private static final String PAUSE_VPN = "de.blinkt.openvpn.PAUSE_VPN";
    private static final String RESUME_VPN = "com.wxy.vpn2018.RESUME_VPN";
    private static final int PRIORITY_MIN = -2;
    private static final int PRIORITY_DEFAULT = 0;
    private static final int PRIORITY_MAX = 2;
    private static boolean mNotificationAlwaysVisible = false;
    private static Class mNotificationActivityClass;
    private final Vector<String> mDnslist = new Vector<>();
    private final NetworkSpace mRoutes = new NetworkSpace();
    private final NetworkSpace mRoutesv6 = new NetworkSpace();
    private final Object mProcessLock = new Object();
    private String lastChannel;
    private Thread mProcessThread = null;
    private VpnProfile mProfile;
    private String mDomain = null;
    private CIDRIP mLocalIP = null;
    private int mMtu;
    private String mLocalIPv6 = null;
    private DeviceStateReceiver mDeviceStateReceiver;
    private boolean mDisplayBytecount = false;
    private boolean mStarting = false;
    private long mConnecttime;
    private boolean mOvpn3 = false;
    private OpenVPNManagement mManagement;


    public static boolean abortConnectionVPN = false;
    CountDownTimer ConnectionTimer;
    long long_usage_today, long_usage_week, long_usage_month, long_usage_now, long_usage_time_today, long_usage_time_total;
    long long_milli_seconds = 0;
    String PREF_USAGE = "daily_usage", TODAY, WEEK, MONTH, YEAR;
    SharedPreferences sp_settings;
    int Random;
    String City, Image;

    private FirebaseAnalytics mFirebaseAnalytics;
    private final IBinder mBinder = new IOpenVPNServiceInternal.Stub() {
        @Override
        public boolean protect(int fd) throws RemoteException {
            return OpenVPNService.this.protect(fd);
        }

        @Override
        public void userPause(boolean shouldbePaused) throws RemoteException {
            OpenVPNService.this.userPause(shouldbePaused);
        }

        @Override
        public boolean stopVPN(boolean replaceConnection) throws RemoteException {
            return OpenVPNService.this.stopVPN(replaceConnection);
        }
    };
    private String mLastTunCfg;
    private String mRemoteGW;
    private Handler guiHandler;
    private Toast mlastToast;
    private Runnable mOpenVPNThread;

    // From: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public static String humanReadableByteCount(long bytes, boolean speed, Resources res) {
        if (speed) bytes = bytes * 8;
        int unit = speed ? 1000 : 1024;
        int exp = Math.max(0, Math.min((int) (Math.log(bytes) / Math.log(unit)), 3));
        float bytesUnit = (float) (bytes / Math.pow(unit, exp));
        if (speed) switch (exp) {
            case 0:
                return res.getString(R.string.bits_per_second, bytesUnit);
            case 1:
                return res.getString(R.string.kbits_per_second, bytesUnit);
            case 2:
                return res.getString(R.string.mbits_per_second, bytesUnit);
            default:
                return res.getString(R.string.gbits_per_second, bytesUnit);
        }
        else switch (exp) {
            case 0:
                return res.getString(R.string.volume_byte, bytesUnit);
            case 1:
                return res.getString(R.string.volume_kbyte, bytesUnit);
            case 2:
                return res.getString(R.string.volume_mbyte, bytesUnit);
            default:
                return res.getString(R.string.volume_gbyte, bytesUnit);
        }
    }

    /**
     * Sets the activity which should be opened when tapped on the permanent notification tile.
     *
     * @param activityClass The activity class to open
     */
    public static void setNotificationActivityClass(Class<? extends Activity> activityClass) {
        mNotificationActivityClass = activityClass;
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(START_SERVICE)) return mBinder;
        else return super.onBind(intent);
    }

    @Override
    public void onRevoke() {
        VpnStatus.logError(R.string.permission_revoked);
        mManagement.stopVPN(false);
        endVpnService();
    }

    // Similar to revoke but do not try to stop process
    public void processDied() {
        endVpnService();
    }

    private void endVpnService() {
        synchronized (mProcessLock) {
            mProcessThread = null;
        }
        VpnStatus.removeByteCountListener(this);
        unregisterDeviceStateReceiver();
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        mOpenVPNThread = null;
        if (!mStarting) {
            stopForeground(!mNotificationAlwaysVisible);
            if (!mNotificationAlwaysVisible) {
                stopSelf();
                VpnStatus.removeStateListener(this);
            }
        }
    }



    private boolean runningOnAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }



    PendingIntent getGraphPendingIntent() {
        // Let the configure Button show the Log
        // Editor : I'm not sure about this but
        // TODO : Check what the fuck is this.
        Class activityClass = MainActivity.class;
        if (mNotificationActivityClass != null) {
            activityClass = mNotificationActivityClass;
        }
        Intent intent = new Intent(getBaseContext(), activityClass);
        intent.putExtra("PAGE", "graph");
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent startLW = PendingIntent.getActivity(this, 0, intent, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return startLW;
    }

    synchronized void registerDeviceStateReceiver(OpenVPNManagement magnagement) {
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mDeviceStateReceiver = new DeviceStateReceiver(magnagement);
        // Fetch initial network state
        mDeviceStateReceiver.networkStateChange(this);
        registerReceiver(mDeviceStateReceiver, filter);
        VpnStatus.addByteCountListener(mDeviceStateReceiver);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            addLollipopCMListener(); */
    }

    synchronized void unregisterDeviceStateReceiver() {
        if (mDeviceStateReceiver != null) try {
            VpnStatus.removeByteCountListener(mDeviceStateReceiver);
            this.unregisterReceiver(mDeviceStateReceiver);
        } catch (IllegalArgumentException iae) {
            // I don't know why  this happens:
            // java.lang.IllegalArgumentException: Receiver not registered: de.blinkt.openvpn.NetworkSateReceiver@41a61a10
            // Ignore for now ...
            iae.printStackTrace();
        }
        mDeviceStateReceiver = null;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            removeLollipopCMListener();*/
    }

    public void userPause(boolean shouldBePaused) {
        if (mDeviceStateReceiver != null) mDeviceStateReceiver.userPause(shouldBePaused);
    }

    @Override
    public boolean stopVPN(boolean replaceConnection) throws RemoteException {
        if (getManagement() != null){
            ConnectionTimer.cancel();
            return getManagement().stopVPN(replaceConnection);
        }
        else return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        SharedPreferences ConnectionDetails = getSharedPreferences("connection_data", 0);
        City = ConnectionDetails.getString("city", "Select a City");
        Image = ConnectionDetails.getString("image", "unitedstates");

        try {
            startForeground(App.NOTIFICATION_ID, getMyActivityNotification("Tap to open the app"));
        } catch (Exception e){
            Bundle params = new Bundle();
            params.putString("device_id", App.device_id);
            params.putString("exception", "OVPS1" + e.toString());
            mFirebaseAnalytics.logEvent("app_param_error", params);
        }

        Date Today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

        TODAY = df.format(Today);
        WEEK = String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
        MONTH = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
        YEAR = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        sp_settings = getSharedPreferences(PREF_USAGE, 0);
        long_usage_today = sp_settings.getLong(TODAY, 0);
        long_usage_week = sp_settings.getLong(WEEK+YEAR, 0);
        long_usage_month = sp_settings.getLong(MONTH+YEAR, 0);
        long_usage_time_today = sp_settings.getLong(TODAY + "_time", 0);
        long_usage_time_total = sp_settings.getLong("total_time", 0);

        final int min = 10;
        final int max = 15;
        Random = new Random().nextInt((max - min) + 1) + min;
        long_milli_seconds = 0;
        abortConnectionVPN = false;
        try {
            ConnectionTimer = new CountDownTimer(86400_000, 1000) {
                public void onTick(long millisUntilFinished) {
                    //Data.StringCountDown =  "remaining " + millisUntilFinished / 1000 + " seconds";
                    long_milli_seconds = long_milli_seconds + 1000;
                    Data.StringCountDown = String.format(getString(R.string.string_of_two_number),  (long_milli_seconds / (1000*60*60)) % 24) + ":" +
                            String.format(getString(R.string.string_of_two_number), TimeUnit.MILLISECONDS.toMinutes( long_milli_seconds) % 60) + ":" +
                            String.format(getString(R.string.string_of_two_number), (TimeUnit.MILLISECONDS.toSeconds(long_milli_seconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(long_milli_seconds))));
                    int time = Random*1000;
                    if ((long_milli_seconds % time) == 0) {
                        SharedPreferences.Editor editor = sp_settings.edit();
                        editor.putLong(TODAY, long_usage_today + long_usage_now);
                        editor.putLong(WEEK+YEAR, long_usage_week + long_usage_now);
                        editor.putLong(MONTH+YEAR, long_usage_month + long_usage_now);
                        editor.putLong(TODAY + "_time", long_usage_time_today + long_milli_seconds);
                        editor.putLong("total_time", long_usage_time_total + long_milli_seconds);
                        editor.apply();
                        Log.e("random", Random + " " + Random*1000);

                    }

                    if(abortConnectionVPN){
                        try {
                            ConnectionTimer.cancel();
                        } catch (Exception e) {
                            Bundle params = new Bundle();
                            params.putString("device_id", App.device_id);
                            params.putString("exception", "OVPS2" + e.toString());
                            mFirebaseAnalytics.logEvent("app_param_error", params);
                        }
                    }


                }
                public void onFinish() {

                }

            }.start();
        } catch (Exception ignored) {

        }






        if (intent != null && intent.getBooleanExtra(ALWAYS_SHOW_NOTIFICATION, false)) mNotificationAlwaysVisible = true;
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
        guiHandler = new Handler(getMainLooper());
        if (intent != null && PAUSE_VPN.equals(intent.getAction())) {
            if (mDeviceStateReceiver != null) mDeviceStateReceiver.userPause(true);
            return START_NOT_STICKY;
        }
        if (intent != null && RESUME_VPN.equals(intent.getAction())) {
            if (mDeviceStateReceiver != null) mDeviceStateReceiver.userPause(false);
            return START_NOT_STICKY;
        }
        if (intent != null && START_SERVICE.equals(intent.getAction())) return START_NOT_STICKY;
        if (intent != null && START_SERVICE_STICKY.equals(intent.getAction())) {
            return START_REDELIVER_INTENT;
        }
        if (intent != null && intent.hasExtra(getPackageName() + ".profileUUID")) {
            String profileUUID = intent.getStringExtra(getPackageName() + ".profileUUID");
            int profileVersion = intent.getIntExtra(getPackageName() + ".profileVersion", 0);
            // Try for 10s to get current version of the profile
            mProfile = ProfileManager.get(this, profileUUID, profileVersion, 100);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                updateShortCutUsage(mProfile);
            }
        } else {
            /* The intent is null when we are set as always-on or the service has been restarted. */
            mProfile = ProfileManager.getLastConnectedProfile(this);
            VpnStatus.logInfo(R.string.service_restarted);
            /* Got no profile, just stop */
            if (mProfile == null) {
                //Log.d("OpenVPN", "Got no last connected profile on null intent. Assuming always on.");
                mProfile = ProfileManager.getAlwaysOnVPN(this);
                if (mProfile == null) {
                    stopSelf(startId);
                    return START_NOT_STICKY;
                }
            }
            /* Do the asynchronous keychain certificate stuff */
            mProfile.checkForRestart(this);
        }
        /* start the OpenVPN process itself in a background thread */
        new Thread(new Runnable() {
            @Override
            public void run() {
                startOpenVPN();
            }
        }).start();
        ProfileManager.setConnectedVpnProfile(this, mProfile);
        VpnStatus.setConnectedVPNProfile(mProfile.getUUIDString());
        return START_STICKY;
    }

    // TODO
    private Notification getMyActivityNotification(String Description){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        int int_temp;
        switch (Image) {
            case "japan":
                int_temp = R.drawable.ic_flag_japan;
                break;
            case "russia":
                int_temp = R.drawable.ic_flag_russia;
                break;
            case "southkorea":
                int_temp = R.drawable.ic_flag_south_korea;
                break;
            case "thailand":
                int_temp = R.drawable.ic_flag_thailand;
                break;
            case "vietnam":
                int_temp = R.drawable.ic_flag_vietnam;
                break;
            case "unitedstates":
                int_temp = R.drawable.ic_flag_united_states;
                break;
            case "unitedkingdom":
                int_temp = R.drawable.ic_flag_united_kingdom;
                break;
            case "singapore":
                int_temp = R.drawable.ic_flag_singapore;
                break;
            case "france":
                int_temp = R.drawable.ic_flag_france;
                break;
            case "germany":
                int_temp = R.drawable.ic_flag_germany;
                break;
            case "canada":
                int_temp = R.drawable.ic_flag_canada;
                break;
            case "luxemburg":
                int_temp = R.drawable.ic_flag_luxemburg;
                break;
            case "netherlands":
                int_temp = R.drawable.ic_flag_netherlands;
                break;
            case "spain":
                int_temp = R.drawable.ic_flag_spain;
                break;
            case "finland":
                int_temp = R.drawable.ic_flag_finland;
                break;
            case "poland":
                int_temp = R.drawable.ic_flag_poland;
                break;
            case "australia":
                int_temp = R.drawable.ic_flag_australia;
                break;
            case "italy":
                int_temp = R.drawable.ic_flag_italy;
                break;
            default:
                int_temp = R.drawable.ic_flag_unknown_mali;
                break;
        }

        String Title;
        if(App.connection_status == 0){
            Title = "Tap to connect " + City;
        } else if(App.connection_status == 1){
            Title = "Connecting " + City;
        } else if(App.connection_status == 2){
            Title = "Connected " + City;
        } else{
            Title = "Tap to open Buzz VPN";
        }

        return new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(Title)
                .setContentText(Description)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), int_temp))
                .setSmallIcon(int_temp)
                .setContentIntent(pendingIntent)
                .build();

    }

    private void updateNotification(String Description) {
        Notification notification = getMyActivityNotification(Description);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(App.NOTIFICATION_ID, notification);
    }


    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private void updateShortCutUsage(VpnProfile profile) {
        if (profile == null) return;
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        shortcutManager.reportShortcutUsed(profile.getUUIDString());
    }

    private void startOpenVPN() {
        VpnStatus.logInfo(R.string.building_configration);
        VpnStatus.updateStateString("VPN_GENERATE_CONFIG", "", R.string.building_configration, ConnectionStatus.LEVEL_START);
        try {
            mProfile.writeConfigFile(this);
        } catch (IOException e) {
            VpnStatus.logException("Error writing config file", e);
            endVpnService();
            return;
        }
        String nativeLibraryDirectory = getApplicationInfo().nativeLibraryDir;
        // Write OpenVPN binary
        String[] argv = VPNLaunchHelper.buildOpenvpnArgv(this);
        // Set a flag that we are starting a new VPN
        mStarting = true;
        // Stop the previous session by interrupting the thread.
        stopOldOpenVPNProcess();
        // An old running VPN should now be exited
        mStarting = false;
        // Start a new session by creating a new thread.
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this);
        mOvpn3 = prefs.getBoolean("ovpn3", false);
        if (!"ovpn3".equals(BuildConfig.FLAVOR)) mOvpn3 = false;
        // Open the Management Interface
        if (!mOvpn3) {
            // start a Thread that handles incoming messages of the managment socket
            OpenVpnManagementThread ovpnManagementThread = new OpenVpnManagementThread(mProfile, this);
            if (ovpnManagementThread.openManagementInterface(this)) {
                Thread mSocketManagerThread = new Thread(ovpnManagementThread, "OpenVPNManagementThread");
                mSocketManagerThread.start();
                mManagement = ovpnManagementThread;
                VpnStatus.logInfo("started Socket Thread");
            } else {
                endVpnService();
                return;
            }
        }
        Runnable processThread;
        if (mOvpn3) {
            OpenVPNManagement mOpenVPN3 = instantiateOpenVPN3Core();
            processThread = (Runnable) mOpenVPN3;
            mManagement = mOpenVPN3;
        } else {
            processThread = new OpenVPNThread(this, argv, nativeLibraryDirectory);
            mOpenVPNThread = processThread;
        }
        synchronized (mProcessLock) {
            mProcessThread = new Thread(processThread, "OpenVPNProcessThread");
            mProcessThread.start();
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mDeviceStateReceiver != null) unregisterDeviceStateReceiver();
                registerDeviceStateReceiver(mManagement);
            }
        });
    }

    private void stopOldOpenVPNProcess() {
        if (mManagement != null) {
            if (mOpenVPNThread != null) {
                ((OpenVPNThread) mOpenVPNThread).setReplaceConnection();
            }
            if (mManagement.stopVPN(true)) {
                // an old was asked to exit, wait 1s
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        forceStopOpenVpnProcess();
    }

    public void forceStopOpenVpnProcess() {
        synchronized (mProcessLock) {
            if (mProcessThread != null) {
                mProcessThread.interrupt();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }

    private OpenVPNManagement instantiateOpenVPN3Core() {
        try {
            Class cl = Class.forName("de.blinkt.openvpn.core.OpenVPNThreadv3");
            return (OpenVPNManagement) cl.getConstructor(OpenVPNService.class, VpnProfile.class).newInstance(this, mProfile);
        } catch (IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IBinder asBinder() {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        synchronized (mProcessLock) {
            if (mProcessThread != null) {
                mManagement.stopVPN(true);
            }
        }
        if (mDeviceStateReceiver != null) {
            this.unregisterReceiver(mDeviceStateReceiver);
        }
        // Just in case unregister for state
        VpnStatus.removeStateListener(this);
        VpnStatus.flushLog();
    }

    private String getTunConfigString() {
        // The format of the string is not important, only that
        // two identical configurations produce the same result
        String cfg = "TUNCFG UNQIUE STRING ips:";
        if (mLocalIP != null) cfg += mLocalIP.toString();
        if (mLocalIPv6 != null) cfg += mLocalIPv6;
        cfg += "routes: " + TextUtils.join("|", mRoutes.getNetworks(true)) + TextUtils.join("|", mRoutesv6.getNetworks(true));
        cfg += "excl. routes:" + TextUtils.join("|", mRoutes.getNetworks(false)) + TextUtils.join("|", mRoutesv6.getNetworks(false));
        cfg += "dns: " + TextUtils.join("|", mDnslist);
        cfg += "domain: " + mDomain;
        cfg += "mtu: " + mMtu;
        return cfg;
    }

    public ParcelFileDescriptor openTun() {
        //Debug.startMethodTracing(getExternalFilesDir(null).toString() + "/opentun.trace", 40* 1024 * 1024);
        Builder builder = new Builder();
        VpnStatus.logInfo(R.string.last_openvpn_tun_config);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mProfile.mAllowLocalLAN) {
            allowAllAFFamilies(builder);
        }
        if (mLocalIP == null && mLocalIPv6 == null) {
            VpnStatus.logError(getString(R.string.opentun_no_ipaddr));
            return null;
        }
        if (mLocalIP != null) {
            addLocalNetworksToRoutes();
            try {
                builder.addAddress(mLocalIP.mIp, mLocalIP.len);
            } catch (IllegalArgumentException iae) {
                VpnStatus.logError(R.string.dns_add_error, mLocalIP, iae.getLocalizedMessage());
                return null;
            }
        }
        if (mLocalIPv6 != null) {
            String[] ipv6parts = mLocalIPv6.split("/");
            try {
                builder.addAddress(ipv6parts[0], Integer.parseInt(ipv6parts[1]));
            } catch (IllegalArgumentException iae) {
                VpnStatus.logError(R.string.ip_add_error, mLocalIPv6, iae.getLocalizedMessage());
                return null;
            }
        }
        for (String dns : mDnslist) {
            try {
                builder.addDnsServer(dns);
            } catch (IllegalArgumentException iae) {
                VpnStatus.logError(R.string.dns_add_error, dns, iae.getLocalizedMessage());
            }
        }
        String release = Build.VERSION.RELEASE;
        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !release.startsWith("4.4.3") && !release.startsWith("4.4.4") && !release.startsWith("4.4.5") && !release.startsWith("4.4.6")) && mMtu < 1280) {
            VpnStatus.logInfo(String.format(Locale.US, "Forcing MTU to 1280 instead of %d to workaround Android Bug #70916", mMtu));
            builder.setMtu(1280);
        } else {
            builder.setMtu(mMtu);
        }
        Collection<ipAddress> positiveIPv4Routes = mRoutes.getPositiveIPList();
        Collection<ipAddress> positiveIPv6Routes = mRoutesv6.getPositiveIPList();
        if ("samsung".equals(Build.BRAND) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDnslist.size() >= 1) {
            // Check if the first DNS Server is in the VPN range
            try {
                ipAddress dnsServer = new ipAddress(new CIDRIP(mDnslist.get(0), 32), true);
                boolean dnsIncluded = false;
                for (ipAddress net : positiveIPv4Routes) {
                    if (net.containsNet(dnsServer)) {
                        dnsIncluded = true;
                    }
                }
                if (!dnsIncluded) {
                    String samsungwarning = String.format("Warning Samsung Android 5.0+ devices ignore DNS servers outside the VPN range. To enable DNS resolution a route to your DNS Server (%s) has been added.", mDnslist.get(0));
                    VpnStatus.logWarning(samsungwarning);
                    positiveIPv4Routes.add(dnsServer);
                }
            } catch (Exception e) {
                VpnStatus.logError("Error parsing DNS Server IP: " + mDnslist.get(0));
            }
        }
        ipAddress multicastRange = new ipAddress(new CIDRIP("224.0.0.0", 3), true);
        for (NetworkSpace.ipAddress route : positiveIPv4Routes) {
            try {
                if (multicastRange.containsNet(route)) VpnStatus.logDebug(R.string.ignore_multicast_route, route.toString());
                else builder.addRoute(route.getIPv4Address(), route.networkMask);
            } catch (IllegalArgumentException ia) {
                VpnStatus.logError(getString(R.string.route_rejected) + route + " " + ia.getLocalizedMessage());
            }
        }
        for (NetworkSpace.ipAddress route6 : positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.networkMask);
            } catch (IllegalArgumentException ia) {
                VpnStatus.logError(getString(R.string.route_rejected) + route6 + " " + ia.getLocalizedMessage());
            }
        }
        if (mDomain != null) builder.addSearchDomain(mDomain);
        VpnStatus.logInfo(R.string.local_ip_info, mLocalIP.mIp, mLocalIP.len, mLocalIPv6, mMtu);
        VpnStatus.logInfo(R.string.dns_server_info, TextUtils.join(", ", mDnslist), mDomain);
        VpnStatus.logInfo(R.string.routes_info_incl, TextUtils.join(", ", mRoutes.getNetworks(true)), TextUtils.join(", ", mRoutesv6.getNetworks(true)));
        VpnStatus.logInfo(R.string.routes_info_excl, TextUtils.join(", ", mRoutes.getNetworks(false)), TextUtils.join(", ", mRoutesv6.getNetworks(false)));
        VpnStatus.logDebug(R.string.routes_debug, TextUtils.join(", ", positiveIPv4Routes), TextUtils.join(", ", positiveIPv6Routes));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAllowedVpnPackages(builder);
        }
        String session = mProfile.mConnections[0].mServerName;
        if (mLocalIP != null && mLocalIPv6 != null) session = getString(R.string.session_ipv6string, session, mLocalIP, mLocalIPv6);
        else if (mLocalIP != null) session = getString(R.string.session_ipv4string, session, mLocalIP);
        builder.setSession(session);
        // No DNS Server, log a warning
        if (mDnslist.size() == 0) VpnStatus.logInfo(R.string.warn_no_dns);
        mLastTunCfg = getTunConfigString();
        // Reset information
        mDnslist.clear();
        mRoutes.clear();
        mRoutesv6.clear();
        mLocalIP = null;
        mLocalIPv6 = null;
        mDomain = null;
        builder.setConfigureIntent(getGraphPendingIntent());
        try {
            //Debug.stopMethodTracing();
            ParcelFileDescriptor tun = builder.establish();
            if (tun == null) throw new NullPointerException("Android establish() method returned null (Really broken network configuration?)");
            return tun;
        } catch (Exception e) {
            VpnStatus.logError(R.string.tun_open_error);
            VpnStatus.logError(getString(R.string.error) + e.getLocalizedMessage());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                VpnStatus.logError(R.string.tun_error_helpful);
            }
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void allowAllAFFamilies(Builder builder) {
        builder.allowFamily(OsConstants.AF_INET);
        builder.allowFamily(OsConstants.AF_INET6);
    }

    private void addLocalNetworksToRoutes() {
        // Add local network interfaces
        String[] localRoutes = NativeUtils.getIfconfig();
        // The format of mLocalRoutes is kind of broken because I don't really like JNI
        for (int i = 0; i < localRoutes.length; i += 3) {
            String intf = localRoutes[i];
            String ipAddr = localRoutes[i + 1];
            String netMask = localRoutes[i + 2];
            if (intf == null || intf.equals("lo") || intf.startsWith("tun") || intf.startsWith("rmnet")) continue;
            if (ipAddr == null || netMask == null) {
                VpnStatus.logError("Local routes are broken?! (Report to author) " + TextUtils.join("|", localRoutes));
                continue;
            }
            if (ipAddr.equals(mLocalIP.mIp)) continue;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !mProfile.mAllowLocalLAN) {
                mRoutes.addIPSplit(new CIDRIP(ipAddr, netMask), true);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mProfile.mAllowLocalLAN) mRoutes.addIP(new CIDRIP(ipAddr, netMask), false);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setAllowedVpnPackages(Builder builder) {
        boolean atLeastOneAllowedApp = false;
        for (String pkg : mProfile.mAllowedAppsVpn) {
            try {
                if (mProfile.mAllowedAppsVpnAreDisallowed) {
                    builder.addDisallowedApplication(pkg);
                } else {
                    builder.addAllowedApplication(pkg);
                    atLeastOneAllowedApp = true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                mProfile.mAllowedAppsVpn.remove(pkg);
                VpnStatus.logInfo(R.string.app_no_longer_exists, pkg);
            }
        }
        if (!mProfile.mAllowedAppsVpnAreDisallowed && !atLeastOneAllowedApp) {
            VpnStatus.logDebug(R.string.no_allowed_app, getPackageName());
            try {
                builder.addAllowedApplication(getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                VpnStatus.logError("This should not happen: " + e.getLocalizedMessage());
            }
        }
        if (mProfile.mAllowedAppsVpnAreDisallowed) {
            VpnStatus.logDebug(R.string.disallowed_vpn_apps_info, TextUtils.join(", ", mProfile.mAllowedAppsVpn));
        } else {
            VpnStatus.logDebug(R.string.allowed_vpn_apps_info, TextUtils.join(", ", mProfile.mAllowedAppsVpn));
        }
    }

    public void addDNS(String dns) {
        mDnslist.add(dns);
    }

    public void setDomain(String domain) {
        if (mDomain == null) {
            mDomain = domain;
        }
    }

    /**
     * Route that is always included, used by the v3 core
     */
    public void addRoute(CIDRIP route) {
        mRoutes.addIP(route, true);
    }

    public void addRoute(String dest, String mask, String gateway, String device) {
        CIDRIP route = new CIDRIP(dest, mask);
        boolean include = isAndroidTunDevice(device);
        NetworkSpace.ipAddress gatewayIP = new NetworkSpace.ipAddress(new CIDRIP(gateway, 32), false);
        if (mLocalIP == null) {
            VpnStatus.logError("Local IP address unset and received. Neither pushed server config nor local config specifies an IP addresses. Opening tun device is most likely going to fail.");
            return;
        }
        NetworkSpace.ipAddress localNet = new NetworkSpace.ipAddress(mLocalIP, true);
        if (localNet.containsNet(gatewayIP)) include = true;
        if (gateway != null && (gateway.equals("255.255.255.255") || gateway.equals(mRemoteGW))) include = true;
        if (route.len == 32 && !mask.equals("255.255.255.255")) {
            VpnStatus.logWarning(R.string.route_not_cidr, dest, mask);
        }
        if (route.normalise()) VpnStatus.logWarning(R.string.route_not_netip, dest, route.len, route.mIp);
        mRoutes.addIP(route, include);
    }

    public void addRoutev6(String network, String device) {
        String[] v6parts = network.split("/");
        boolean included = isAndroidTunDevice(device);
        // Tun is opened after ROUTE6, no device name may be present
        try {
            Inet6Address ip = (Inet6Address) InetAddress.getAllByName(v6parts[0])[0];
            int mask = Integer.parseInt(v6parts[1]);
            mRoutesv6.addIPv6(ip, mask, included);
        } catch (UnknownHostException e) {
            VpnStatus.logException(e);
        }
    }

    private boolean isAndroidTunDevice(String device) {
        return device != null && (device.startsWith("tun") || "(null)".equals(device) || "vpnservice-tun".equals(device));
    }

    public void setMtu(int mtu) {
        mMtu = mtu;
    }

    public void setLocalIP(CIDRIP cdrip) {
        mLocalIP = cdrip;
    }

    public void setLocalIP(String local, String netmask, int mtu, String mode) {
        mLocalIP = new CIDRIP(local, netmask);
        mMtu = mtu;
        mRemoteGW = null;
        long netMaskAsInt = CIDRIP.getInt(netmask);
        if (mLocalIP.len == 32 && !netmask.equals("255.255.255.255")) {
            // get the netmask as IP
            int masklen;
            long mask;
            if ("net30".equals(mode)) {
                masklen = 30;
                mask = 0xfffffffc;
            } else {
                masklen = 31;
                mask = 0xfffffffe;
            }
            // Netmask is Ip address +/-1, assume net30/p2p with small net
            if ((netMaskAsInt & mask) == (mLocalIP.getInt() & mask)) {
                mLocalIP.len = masklen;
            } else {
                mLocalIP.len = 32;
                if (!"p2p".equals(mode)) VpnStatus.logWarning(R.string.ip_not_cidr, local, netmask, mode);
            }
        }
        if (("p2p".equals(mode) && mLocalIP.len < 32) || ("net30".equals(mode) && mLocalIP.len < 30)) {
            VpnStatus.logWarning(R.string.ip_looks_like_subnet, local, netmask, mode);
        }
        /* Workaround for Lollipop, it  does not route traffic to the VPNs own network mask */
        if (mLocalIP.len <= 31 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CIDRIP interfaceRoute = new CIDRIP(mLocalIP.mIp, mLocalIP.len);
            interfaceRoute.normalise();
            addRoute(interfaceRoute);
        }
        // Configurations are sometimes really broken...
        mRemoteGW = netmask;
    }

    public void setLocalIPv6(String ipv6addr) {
        mLocalIPv6 = ipv6addr;
    }

    @Override
    public void updateState(String state, String logmessage, int resid, ConnectionStatus level) {
        // If the process is not running, ignore any state,
        // Notification should be invisible in this state
        doSendBroadcast(state, level);
        if (mProcessThread == null && !mNotificationAlwaysVisible) return;
        String channel = NOTIFICATION_CHANNEL_NEWSTATUS_ID;
        // Display byte count only after being connected
        {
            if (level == LEVEL_WAITING_FOR_USER_INPUT) {
                // The user is presented a dialog of some kind, no need to inform the user
                // with a notifcation
                return;
            } else if (level == LEVEL_CONNECTED) {
                mDisplayBytecount = true;
                mConnecttime = System.currentTimeMillis();
                if (!runningOnAndroidTV()) channel = NOTIFICATION_CHANNEL_BG_ID;
            } else {
                mDisplayBytecount = false;
            }
            // Other notifications are shown,
            // This also mean we are no longer connected, ignore bytecount messages until next
            // CONNECTED
            // Does not work :(
            //showNotification(VpnStatus.getLastCleanLogMessage(this), VpnStatus.getLastCleanLogMessage(this), channel, 0, level);



        }
    }

    @Override
    public void setConnectedVPN(String uuid) {
    }

    private void doSendBroadcast(String state, ConnectionStatus level) {
        Intent vpnstatus = new Intent();
        vpnstatus.setAction("de.blinkt.openvpn.VPN_STATUS");
        vpnstatus.putExtra("status", level.toString());
        vpnstatus.putExtra("detailstatus", state);
        sendBroadcast(vpnstatus, permission.ACCESS_NETWORK_STATE);
    }

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {

        if (mDisplayBytecount) {
            final long Total = in + out;
            String StringDown, StringUp;
            if(diffIn < 1000){
                StringDown = diffIn + " byte/s";
            }  else if ((diffIn >= 1000) && (diffIn <= 1000_000)) {
                StringDown = diffIn/1000 +  " kb/s";
            } else {
                StringDown = diffIn/1000_000 + " mb/s";
            }

            if(diffOut < 1000){
                StringUp = diffOut + " byte/s";
            }  else if ((diffOut >= 1000) && (diffOut <= 1000_000)) {
                StringUp = diffOut/1000 + " kb/s";
            } else {
                StringUp = diffOut/1000_000 + " mb/s";
            }

            // save data
            long_usage_now = Total;
            LongDataUsage = Total;

            String Stat = "Down: " +  StringDown + " Up: " + StringUp;
            try{
                updateNotification(Stat);
            } catch (Exception e){
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "OVPS3" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        Runnable r = msg.getCallback();
        if (r != null) {
            r.run();
            return true;
        } else {
            return false;
        }
    }

    public OpenVPNManagement getManagement() {
        return mManagement;
    }

    public String getTunReopenStatus() {
        String currentConfiguration = getTunConfigString();
        if (currentConfiguration.equals(mLastTunCfg)) {
            return "NOACTION";
        } else {
            String release = Build.VERSION.RELEASE;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !release.startsWith("4.4.3") && !release.startsWith("4.4.4") && !release.startsWith("4.4.5") && !release.startsWith("4.4.6"))
                // There will be probably no 4.4.4 or 4.4.5 version, so don't waste effort to do parsing here
                return "OPEN_AFTER_CLOSE";
            else return "OPEN_BEFORE_CLOSE";
        }
    }

    public void requestInputFromUser(int resid, String needed) {
        VpnStatus.updateStateString("NEED", "need " + needed, resid, LEVEL_WAITING_FOR_USER_INPUT);
        //showNotification(getString(resid), getString(resid), NOTIFICATION_CHANNEL_NEWSTATUS_ID, 0, LEVEL_WAITING_FOR_USER_INPUT);
    }
}
