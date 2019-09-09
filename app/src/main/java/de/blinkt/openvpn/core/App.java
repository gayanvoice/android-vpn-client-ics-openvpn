/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package de.blinkt.openvpn.core;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import android.util.Log;

import com.buzz.vpn.R;

import java.util.Calendar;
import java.util.Random;


public class App extends /*com.orm.SugarApp*/ Application {
    public static boolean isStart;
    public static int connection_status = 0;
    public static boolean hasFile = false;
    public static boolean abortConnection = false;
    public static long CountDown;
    public static boolean ShowDailyUsage = true;
    public static String device_id;
    public static long device_created;
    public static final String CHANNEL_ID = "COM.BUZZ.VPN";
    public static final int NOTIFICATION_ID = new Random().nextInt(601) + 200;
    NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();



        SharedPreferences sp_settings = getSharedPreferences("settings_data", 0);
        device_id = sp_settings.getString("device_id", "NULL");

        if(device_id.equals("NULL")){
            device_id = getUniqueKey();
            SharedPreferences.Editor Editor = sp_settings.edit();
            Editor.putString("device_id", device_id);
            Editor.putString("device_created", String.valueOf(System.currentTimeMillis()));
            Editor.apply();

        }

        PRNGFixes.apply();
        StatusListener mStatus = new StatusListener();
        mStatus.init(getApplicationContext());

    }

    private void createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "COMBUZZVPN",
                        NotificationManager.IMPORTANCE_LOW
                );

                serviceChannel.setSound(null, null);
                manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
        } catch (Exception e){
            //Log.e("error", e.getStackTrace()[0].getMethodName());
        }
    }

    private String getUniqueKey() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        String Time = getResources().getString(R.string.get_time, year, month, day, hour, minute, second, millis);

        String str_api = String.valueOf(android.os.Build.VERSION.SDK_INT); // API
        String str_model=  String.valueOf(Build.MODEL); // Model
        String str_manufacturer = String.valueOf(Build.MANUFACTURER); // Manufacturer
        String version;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "00";
        }

        Log.e("key", Time + str_manufacturer+str_api+str_model+version);
        return Time + str_manufacturer+str_api+str_model+version;
    }



}
