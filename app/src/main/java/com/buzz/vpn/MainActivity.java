package com.buzz.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

import static com.buzz.vpn.Data.isAppDetails;
import static com.buzz.vpn.Data.isConnectionDetails;


public class MainActivity extends AppCompatActivity implements VpnStatus.ByteCountListener, VpnStatus.StateListener {


    IOpenVPNServiceInternal mService;

    InputStream inputStream;
    BufferedReader bufferedReader;
    ConfigParser cp;
    VpnProfile vp;
    ProfileManager pm;
    Thread thread;

    // NEW
    ImageView iv_home, iv_servers, iv_data, iv_progress_bar;
    LinearLayout ll_text_bubble, ll_main_data, ll_main_today;
    TextView tv_message_top_text, tv_message_bottom_text, tv_data_text, tv_data_name;
    TextView tv_data_today, tv_data_today_text, tv_data_today_name;

    Button btn_connection;
    LottieAnimationView la_animation;

    Animation fade_in_1000, fade_out_1000;

    boolean EnableConnectButton = false;

    int progress = 0;

    String TODAY;

    CountDownTimer ConnectionTimer;
    TextView tv_main_count_down;

    // new
    boolean hasFile = false;
    String FileID = "NULL", File = "NULL", City = "NULL", Image = "NULL";
    String DarkMode = "false";

    ConstraintLayout constLayoutMain;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    @Override
    protected void onStop() {
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences SettingsDetails = getSharedPreferences("settings_data", 0);
        DarkMode = SettingsDetails.getString("dark_mode", "false");
        constLayoutMain = findViewById(R.id.constraintLayoutMain);
        if (DarkMode.equals("true")) {
            constLayoutMain.setBackgroundColor(getResources().getColor(R.color.colorDarkBackground));
            iv_home.setImageResource(R.drawable.ic_home_white);
            iv_servers.setImageResource(R.drawable.ic_go_forward_white);
        } else {
            constLayoutMain.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
            iv_home.setImageResource(R.drawable.ic_home);
            iv_servers.setImageResource(R.drawable.ic_go_forward);
        }

        if (!isAppDetails && !isConnectionDetails) {
            try {
                Intent Welcome = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(Welcome);
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA1" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }
        }


        EncryptData En = new EncryptData();
        SharedPreferences ConnectionDetails = getSharedPreferences("connection_data", 0);
        FileID = ConnectionDetails.getString("file_id", "NA");
        File = En.decrypt(ConnectionDetails.getString("file", "NA"));
        City = ConnectionDetails.getString("city", "NA");
        Image = ConnectionDetails.getString("image", "NA");

        if (!FileID.isEmpty()) {
            hasFile = true;
        } else {
            hasFile = false;
        }

        //Log.e("connection_file", Data.FileString);


        try {
            VpnStatus.addStateListener(this);
            VpnStatus.addByteCountListener(this);
            Intent intent = new Intent(this, OpenVPNService.class);
            intent.setAction(OpenVPNService.START_SERVICE);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", App.device_id);
            params.putString("exception", "MA2" + e.toString());
            mFirebaseAnalytics.logEvent("app_param_error", params);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Date Today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        TODAY = df.format(Today);

        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface RobotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface RobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

        iv_home = findViewById(R.id.iv_home);
        iv_servers = findViewById(R.id.iv_servers);
        iv_data = findViewById(R.id.iv_data);
        ll_text_bubble = findViewById(R.id.ll_text_bubble);
        ll_main_data = findViewById(R.id.ll_main_data);
        ll_main_today = findViewById(R.id.ll_main_today);
        tv_message_top_text = findViewById(R.id.tv_message_top_text);
        tv_message_bottom_text = findViewById(R.id.tv_message_bottom_text);
        tv_data_text = findViewById(R.id.tv_data_text);
        tv_data_name = findViewById(R.id.tv_data_name);
        btn_connection = findViewById(R.id.btn_connection);
        la_animation = findViewById(R.id.la_animation);
        tv_data_today = findViewById(R.id.tv_data_today);
        tv_data_today_text = findViewById(R.id.tv_data_today_text);
        tv_data_today_name = findViewById(R.id.tv_data_today_name);
        tv_main_count_down = findViewById(R.id.tv_main_count_down);

        fade_in_1000 = AnimationUtils.loadAnimation(this, R.anim.fade_in_1000);
        fade_out_1000 = AnimationUtils.loadAnimation(this, R.anim.fade_out_1000);

        ll_text_bubble.setAnimation(fade_in_1000);

        tv_message_top_text.setTypeface(RobotoMedium);
        tv_message_bottom_text.setTypeface(RobotoMedium);

        tv_main_count_down.setTypeface(RobotoBold);
        btn_connection.setTypeface(RobotoBold);

        tv_data_text.setTypeface(RobotoRegular);
        tv_data_name.setTypeface(RobotoMedium);

        tv_data_today.setTypeface(RobotoMedium);
        tv_data_today_text.setTypeface(RobotoRegular);
        tv_data_today_name.setTypeface(RobotoMedium);

        LinearLayout linearLayoutMainHome = findViewById(R.id.linearLayoutMainHome);
        linearLayoutMainHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Data.isAppDetails) {
                    Intent About = new Intent(MainActivity.this, UsageActivity.class);
                    startActivity(About);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                }
            }
        });

        LinearLayout linearLayoutMainServers = findViewById(R.id.linearLayoutMainServers);
        linearLayoutMainServers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Data.isConnectionDetails) {
                    Intent Servers = new Intent(MainActivity.this, ServerActivity.class);
                    startActivity(Servers);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                }
            }
        });

        final Handler handlerToday = new Handler();
        handlerToday.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(MainActivity.this, R.id.linearLayoutMainHome, R.anim.anim_slide_down, true);
                startAnimation(MainActivity.this, R.id.linearLayoutMainServers, R.anim.anim_slide_down, true);
            }
        }, 1000);

        btn_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (!App.isStart) {
                            if (!hasFile) {
                                Intent Servers = new Intent(MainActivity.this, ServerActivity.class);
                                startActivity(Servers);
                                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                            } else {
                                if (hasInternetConnection()) {
                                    try {
                                        start_vpn(File);
                                        final Handler handlerToday = new Handler();
                                        handlerToday.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_down_800, false);
                                            }
                                        }, 500);

                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);

                                        startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                        la_animation.cancelAnimation();
                                        la_animation.setAnimation(R.raw.conneting);
                                        la_animation.playAnimation();


                                        iv_progress_bar = findViewById(R.id.iv_progress_bar);
                                        iv_progress_bar.getLayoutParams().width = 10;
                                        progress = 10;
                                        startAnimation(MainActivity.this, R.id.iv_progress_bar, R.anim.fade_in_1000, true);

                                        tv_main_count_down.setVisibility(View.VISIBLE);
                                        App.CountDown = 30;
                                        try {
                                            ConnectionTimer = new CountDownTimer(32_000, 1000) {
                                                public void onTick(long millisUntilFinished) {
                                                    App.CountDown = App.CountDown - 1;

                                                    iv_progress_bar.getLayoutParams().width = progress;
                                                    progress = progress + (int) getResources().getDimension(R.dimen.lo_10dpGrid);
                                                    tv_main_count_down.setText(String.valueOf(App.CountDown));

                                                    if (App.connection_status == 2) {
                                                        ConnectionTimer.cancel();
                                                        SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
                                                        SharedPreferences.Editor Editor = SharedAppDetails.edit();
                                                        Editor.putString("connection_time", String.valueOf(App.CountDown));
                                                        Editor.apply();
                                                        if (App.CountDown >= 20) {
                                                            SharedPreferences settings = getSharedPreferences("settings_data", 0);
                                                            String Rate = settings.getString("rate", "false");
                                                            if (Rate.equals("false")) {
                                                                Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Intent Servers = new Intent(MainActivity.this, ReviewActivity.class);
                                                                        startActivity(Servers);
                                                                        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                                                                    }
                                                                }, 1000);
                                                            }
                                                        }

                                                        startAnimation(MainActivity.this, R.id.tv_main_count_down, R.anim.fade_out_1000, false);
                                                        startAnimation(MainActivity.this, R.id.iv_progress_bar, R.anim.fade_out_1000, false);
                                                        startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_out_1000, false);
                                                    }

                                                    if (App.CountDown <= 20) {
                                                        EnableConnectButton = true;
                                                    }

                                                    if (App.CountDown <= 1) {
                                                        ConnectionTimer.cancel();
                                                        startAnimation(MainActivity.this, R.id.tv_main_count_down, R.anim.fade_out_500, false);
                                                        startAnimation(MainActivity.this, R.id.iv_progress_bar, R.anim.fade_out_500, false);
                                                        startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_out_500, false);

                                                        try {
                                                            stop_vpn();

                                                            final Handler handlerToday = new Handler();
                                                            handlerToday.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_down_800, false);
                                                                }
                                                            }, 500);

                                                            final Handler handlerData = new Handler();
                                                            handlerData.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                                                }
                                                            }, 1000);

                                                            startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                                            la_animation.cancelAnimation();
                                                            la_animation.setAnimation(R.raw.ninjainsecure);
                                                            la_animation.playAnimation();

                                                            App.ShowDailyUsage = true;
                                                        } catch (Exception e) {
                                                            Bundle params = new Bundle();
                                                            params.putString("device_id", App.device_id);
                                                            params.putString("exception", "MA3" + e.toString());
                                                            mFirebaseAnalytics.logEvent("app_param_error", params);
                                                        }
                                                        App.isStart = false;
                                                    }

                                                }

                                                public void onFinish() {

                                                }

                                            };
                                        } catch (Exception e) {
                                            Bundle params = new Bundle();
                                            params.putString("device_id", App.device_id);
                                            params.putString("exception", "MA4" + e.toString());
                                            mFirebaseAnalytics.logEvent("app_param_error", params);
                                        }
                                        ConnectionTimer.start();

                                        EnableConnectButton = false;
                                        App.isStart = true;

                                    } catch (Exception e) {
                                        Bundle params = new Bundle();
                                        params.putString("device_id", App.device_id);
                                        params.putString("exception", "MA5" + e.toString());
                                        mFirebaseAnalytics.logEvent("app_param_error", params);
                                    }

                                }
                            }
                        } else {
                            if (EnableConnectButton) {
                                try {
                                    stop_vpn();
                                    try {
                                        ConnectionTimer.cancel();
                                    } catch (Exception ignored) {
                                        //new SyncFunctions(MainActivity.this, "MA6 " +  e.toString()).set_error_log();
                                    }

                                    try {
                                        iv_progress_bar.setVisibility(View.INVISIBLE);
                                        tv_main_count_down.setVisibility(View.INVISIBLE);
                                    } catch (Exception ignored) {
                                        //new SyncFunctions(MainActivity.this, "MA7 " +  e.toString()).set_error_log();
                                    }

                                    final Handler handlerToday = new Handler();
                                    handlerToday.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_down_800, false);
                                            ll_main_data.setVisibility(View.INVISIBLE);
                                        }
                                    }, 500);


                                    final Handler handlerData = new Handler();
                                    handlerData.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                        }
                                    }, 1000);

                                    startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                    la_animation.cancelAnimation();
                                    la_animation.setAnimation(R.raw.ninjainsecure);
                                    la_animation.playAnimation();

                                    SharedPreferences settings = getSharedPreferences("settings_data", 0);
                                    String ConnectionTime = settings.getString("connection_time", "0");
                                    if (Long.valueOf(ConnectionTime) >= 20) {
                                        SharedPreferences.Editor Editor = settings.edit();
                                        Editor.putString("connection_time", "0");
                                        Editor.apply();
                                        String Rate = settings.getString("rate", "false");
                                        if (Rate.equals("false")) {
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent Servers = new Intent(MainActivity.this, ReviewActivity.class);
                                                    startActivity(Servers);
                                                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);

                                                }
                                            }, 500);
                                        }
                                    }


                                    App.ShowDailyUsage = true;
                                } catch (Exception e) {
                                    Bundle params = new Bundle();
                                    params.putString("device_id", App.device_id);
                                    params.putString("exception", "MA6" + e.toString());
                                    mFirebaseAnalytics.logEvent("app_param_error", params);
                                }
                                App.isStart = false;
                            }
                        }
                    }
                };
                r.run();
            }

        });


        // ui refresh
        thread = new Thread() {
            boolean ShowData = true;
            boolean ShowAnimation = true;

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // set country flag
                                if (App.abortConnection) {
                                    App.abortConnection = false;

                                    if (App.connection_status != 2) {
                                        App.CountDown = 1;
                                    }

                                    if (App.connection_status == 2) {
                                        try {
                                            stop_vpn();
                                            try {
                                                ConnectionTimer.cancel();
                                            } catch (Exception e) {
                                                Bundle params = new Bundle();
                                                params.putString("device_id", App.device_id);
                                                params.putString("exception", "MA7" + e.toString());
                                                mFirebaseAnalytics.logEvent("app_param_error", params);
                                            }

                                            iv_progress_bar.setVisibility(View.INVISIBLE);
                                            tv_main_count_down.setVisibility(View.INVISIBLE);

                                            final Handler handlerToday = new Handler();
                                            handlerToday.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_down_800, false);
                                                    ll_main_data.setVisibility(View.INVISIBLE);
                                                }
                                            }, 500);


                                            final Handler handlerData = new Handler();
                                            handlerData.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                                }
                                            }, 1000);

                                            startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                            la_animation.cancelAnimation();
                                            la_animation.setAnimation(R.raw.ninjainsecure);
                                            la_animation.playAnimation();

                                            App.ShowDailyUsage = true;
                                        } catch (Exception e) {
                                            Bundle params = new Bundle();
                                            params.putString("device_id", App.device_id);
                                            params.putString("exception", "MA8" + e.toString());
                                            mFirebaseAnalytics.logEvent("app_param_error", params);
                                        }
                                        App.isStart = false;
                                    }

                                }

                                switch (Image) {
                                    case "japan":
                                        iv_servers.setImageResource(R.drawable.ic_flag_japan);
                                        break;
                                    case "russia":
                                        iv_servers.setImageResource(R.drawable.ic_flag_russia);
                                        break;
                                    case "southkorea":
                                        iv_servers.setImageResource(R.drawable.ic_flag_south_korea);
                                        break;
                                    case "thailand":
                                        iv_servers.setImageResource(R.drawable.ic_flag_thailand);
                                        break;
                                    case "vietnam":
                                        iv_servers.setImageResource(R.drawable.ic_flag_vietnam);
                                        break;
                                    case "unitedstates":
                                        iv_servers.setImageResource(R.drawable.ic_flag_united_states);
                                        break;
                                    case "unitedkingdom":
                                        iv_servers.setImageResource(R.drawable.ic_flag_united_kingdom);
                                        break;
                                    case "singapore":
                                        iv_servers.setImageResource(R.drawable.ic_flag_singapore);
                                        break;
                                    case "france":
                                        iv_servers.setImageResource(R.drawable.ic_flag_france);
                                        break;
                                    case "germany":
                                        iv_servers.setImageResource(R.drawable.ic_flag_germany);
                                        break;
                                    case "canada":
                                        iv_servers.setImageResource(R.drawable.ic_flag_canada);
                                        break;
                                    case "luxemburg":
                                        iv_servers.setImageResource(R.drawable.ic_flag_luxemburg);
                                        break;
                                    case "netherlands":
                                        iv_servers.setImageResource(R.drawable.ic_flag_netherlands);
                                        break;
                                    case "spain":
                                        iv_servers.setImageResource(R.drawable.ic_flag_spain);
                                        break;
                                    case "finland":
                                        iv_servers.setImageResource(R.drawable.ic_flag_finland);
                                        break;
                                    case "poland":
                                        iv_servers.setImageResource(R.drawable.ic_flag_poland);
                                        break;
                                    case "australia":
                                        iv_servers.setImageResource(R.drawable.ic_flag_australia);
                                        break;
                                    case "italy":
                                        iv_servers.setImageResource(R.drawable.ic_flag_italy);
                                        break;
                                    default:
                                        iv_servers.setImageResource(R.drawable.ic_flag_unknown_mali);
                                        break;

                                }

                                // set connection button
                                if (hasFile) {
                                    if (App.connection_status == 0) {
                                        // disconnected
                                        btn_connection.setText("Connect");
                                        btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_connect));

                                    } else if (App.connection_status == 1) {
                                        // connecting
                                        if (EnableConnectButton) {
                                            btn_connection.setText("Cancel");
                                            btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_retry));
                                        } else {
                                            btn_connection.setText("Connecting");
                                            btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_retry));
                                        }
                                    } else if (App.connection_status == 2) {
                                        // connected
                                        btn_connection.setText("Disconnect");
                                        btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_disconnect));
                                    } else if (App.connection_status == 3) {
                                        // connected
                                        btn_connection.setText("Remove VPN Apps");
                                        btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_retry));

                                    }

                                }

                                // set message text
                                if (hasFile) {
                                    if (hasInternetConnection()) {
                                        if (App.connection_status == 0) {
                                            // disconnected
                                            tv_message_top_text.setText("The connection is ready");
                                            tv_message_bottom_text.setText("Tap CONNECT to start :)");

                                        } else if (App.connection_status == 1) {
                                            // connecting
                                            tv_message_top_text.setText("Connecting " + City);
                                            tv_message_bottom_text.setText(VpnStatus.getLastCleanLogMessage(MainActivity.this));

                                        } else if (App.connection_status == 2) {
                                            // connected
                                            tv_message_top_text.setText("Connected " + City);
                                            tv_message_bottom_text.setText(Data.StringCountDown);


                                        } else if (App.connection_status == 3) {
                                            // connected
                                            tv_message_top_text.setText("Dangerous VPN apps found");
                                            tv_message_bottom_text.setText("Your device at a risk, remove other VPN apps! potential dangerous VPN apps keep blocking internet connection");
                                        }
                                    } else {
                                        tv_message_top_text.setText("Connection is not available");
                                        tv_message_bottom_text.setText("Check your internet connection to continue");
                                    }

                                }

                                // show data limit
                                if (ShowData) {
                                    ShowData = false;
                                    if (App.connection_status == 0) {
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    } else if (App.connection_status == 1) {
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    } else if (App.connection_status == 2) {
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    } else if (App.connection_status == 3) {
                                        // connected
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    }
                                }

                                // get daily usage
                                if (hasFile) {
                                    if (App.connection_status == 0) {
                                        // disconnected
                                        if (App.ShowDailyUsage) {
                                            App.ShowDailyUsage = false;
                                            String PREF_USAGE = "daily_usage";
                                            SharedPreferences settings = getSharedPreferences(PREF_USAGE, 0);
                                            long long_usage_today = settings.getLong(TODAY, 0);

                                            if (long_usage_today < 1000) {
                                                tv_data_today_text.setText("1KB");
                                            } else if ((long_usage_today >= 1000) && (long_usage_today <= 1000_000)) {
                                                tv_data_today_text.setText((long_usage_today / 1000) + "KB");
                                            } else {
                                                tv_data_today_text.setText((long_usage_today / 1000_000) + "MB");
                                            }
                                        }
                                    }
                                }

                                // show animation
                                if (hasFile) {
                                    if (ShowAnimation) {
                                        ShowAnimation = false;
                                        if (App.connection_status == 0) {
                                            // disconnected
                                            startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                            la_animation.cancelAnimation();
                                            la_animation.setAnimation(R.raw.ninjainsecure);
                                            la_animation.playAnimation();

                                        } else if (App.connection_status == 1) {
                                            // connecting
                                            startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                            la_animation.cancelAnimation();
                                            la_animation.setAnimation(R.raw.conneting);
                                            la_animation.playAnimation();

                                        } else if (App.connection_status == 3) {
                                            // connected
                                            startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                                            la_animation.cancelAnimation();
                                            la_animation.setAnimation(R.raw.ninjainsecure);
                                            la_animation.playAnimation();
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "MA9" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        };
        thread.start();

    }

    private boolean hasInternetConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", App.device_id);
            params.putString("exception", "MA10" + e.toString());
            mFirebaseAnalytics.logEvent("app_param_error", params);
        }

        return haveConnectedWifi || haveConnectedMobile;
    }

    private void start_vpn(String VPNFile) {
        SharedPreferences sp_settings;
        sp_settings = getSharedPreferences("daily_usage", 0);
        long connection_today = sp_settings.getLong(TODAY + "_connections", 0);
        long connection_total = sp_settings.getLong("total_connections", 0);
        SharedPreferences.Editor editor = sp_settings.edit();
        editor.putLong(TODAY + "_connections", connection_today + 1);
        editor.putLong("total_connections", connection_total + 1);
        editor.apply();

        Bundle params = new Bundle();
        params.putString("device_id", App.device_id);
        params.putString("city", City);
        mFirebaseAnalytics.logEvent("app_param_country", params);

        App.connection_status = 1;
        try {
            inputStream = null;
            bufferedReader = null;
            try {
                assert VPNFile != null;
                inputStream = new ByteArrayInputStream(VPNFile.getBytes(Charset.forName("UTF-8")));
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA11" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            try { // M8
                assert inputStream != null;
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream/*, Charset.forName("UTF-8")*/));
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA12" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            cp = new ConfigParser();
            try {
                cp.parseConfig(bufferedReader);
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA13" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }
            vp = cp.convertProfile();
            vp.mAllowedAppsVpnAreDisallowed = true;

            EncryptData En = new EncryptData();
            SharedPreferences AppValues = getSharedPreferences("app_values", 0);
            String AppDetailsValues = En.decrypt(AppValues.getString("app_details", "NA"));

            try {
                JSONObject json_response = new JSONObject(AppDetailsValues);
                JSONArray jsonArray = json_response.getJSONArray("blocked");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_object = jsonArray.getJSONObject(i);
                    vp.mAllowedAppsVpn.add(json_object.getString("app"));
                    Log.e("packages", json_object.getString("app"));
                }
            } catch (JSONException e) {
                params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA14" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }


            try {
                vp.mName = Build.MODEL;
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA15" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            vp.mUsername = Data.FileUsername;
            vp.mPassword = Data.FilePassword;

            try {
                pm = ProfileManager.getInstance(MainActivity.this);
                pm.addProfile(vp);
                pm.saveProfileList(MainActivity.this);
                pm.saveProfile(MainActivity.this, vp);
                vp = pm.getProfileByName(Build.MODEL);
                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
                App.isStart = false;
            } catch (Exception e) {
                params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA16" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }
        } catch (Exception e) {
            params = new Bundle();
            params.putString("device_id", App.device_id);
            params.putString("exception", "MA17" + e.toString());
            mFirebaseAnalytics.logEvent("app_param_error", params);
        }
    }

    public void stop_vpn() {
        App.connection_status = 0;
        OpenVPNService.abortConnectionVPN = true;
        ProfileManager.setConntectedVpnProfileDisconnected(this);

        if (mService != null) {

            try {
                mService.stopVPN(false);
            } catch (RemoteException e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA18" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            try {
                pm = ProfileManager.getInstance(this);
                vp = pm.getProfileByName(Build.MODEL);
                pm.removeProfile(this, vp);
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "MA17" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }


        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    // does not need
    @Override
    public void finish() {
        super.finish();
    }


    @Override
    public void updateState(final String state, String logmessage, int localizedResId, ConnectionStatus level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("CONNECTED")) {
                    App.isStart = true;
                    App.connection_status = 2;

                    Handler handlerData = new Handler();
                    handlerData.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAnimation(MainActivity.this, R.id.la_animation, R.anim.fade_in_1000, true);
                            la_animation.cancelAnimation();
                            la_animation.setAnimation(R.raw.ninjasecure);
                            la_animation.playAnimation();
                        }
                    }, 1000);

                    EnableConnectButton = true;
                }
            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {

    }


    @Override
    public void updateByteCount(long ins, long outs, long diffIns, long diffOuts) {
        final long Total = ins + outs;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // size
                if (Total < 1000) {
                    tv_data_text.setText("1KB");
                    tv_data_name.setText("USED");
                } else if ((Total >= 1000) && (Total <= 1000_000)) {
                    tv_data_text.setText((Total / 1000) + "KB");
                    tv_data_name.setText("USED");
                } else {
                    tv_data_text.setText((Total / 1000_000) + "MB");
                    tv_data_name.setText("USED");
                }
            }
        });
    }

    public void startAnimation(Context ctx, int view, int animation, boolean show) {
        final View Element = findViewById(view);
        if (show) {
            Element.setVisibility(View.VISIBLE);
        } else {
            Element.setVisibility(View.INVISIBLE);
        }
        Animation anim = AnimationUtils.loadAnimation(ctx, animation);
        Element.startAnimation(anim);
    }


}


