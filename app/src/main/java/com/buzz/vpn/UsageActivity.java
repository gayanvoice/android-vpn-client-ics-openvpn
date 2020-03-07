package com.buzz.vpn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.blinkt.openvpn.core.App;

//import android.graphics.Color;

public class UsageActivity extends Activity {

    TextView tv_usage_title;
    ImageView iv_go_forward;
    TextView tv_usage_data_title, tv_usage_connection_details, tv_usage_socialmedia_title, tv_usage_appstore_title;
    TextView tv_usage_cu_title, tv_usage_cu_version;
    TextView tv_usage_share_title, tv_usage_share_description;
    TextView tv_usage_app_name, tv_usage_app_copyright;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //AppBrain.addTestDevice("12345");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // 50
        iv_go_forward = findViewById(R.id.iv_go_forward);
        tv_usage_title = findViewById(R.id.tv_usage_title);
        tv_usage_cu_title = findViewById(R.id.tv_usage_cu_title);
        tv_usage_cu_version = findViewById(R.id.tv_usage_cu_version);
        tv_usage_app_name = findViewById(R.id.tv_usage_app_name);
        tv_usage_app_copyright = findViewById(R.id.tv_usage_app_copyright);

        tv_usage_share_title = findViewById(R.id.tv_usage_share_title);
        tv_usage_share_description = findViewById(R.id.tv_usage_share_decription);

        tv_usage_data_title = findViewById(R.id.tv_usage_data_title);
        tv_usage_connection_details = findViewById(R.id.tv_usage_connection_details);
        tv_usage_socialmedia_title = findViewById(R.id.tv_usage_socialmedia_title);
        tv_usage_appstore_title = findViewById(R.id.tv_usage_appstore_title);

        Typeface RobotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface RobotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface RobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

        SharedPreferences ConnectionDetails = getSharedPreferences("app_details", 0);
        String cuVersion = ConnectionDetails.getString("cu_version", "NULL");

        tv_usage_cu_title.setTypeface(RobotoMedium);
        tv_usage_cu_version.setTypeface(RobotoRegular);

        tv_usage_app_name.setTypeface(RobotoRegular);
        tv_usage_app_copyright.setTypeface(RobotoRegular);

        tv_usage_title.setTypeface(RobotoMedium);
        tv_usage_cu_version.setText("App version " + cuVersion);

        tv_usage_data_title.setTypeface(RobotoBold);
        TextView tv_usage_time_title = findViewById(R.id.tv_usage_time_title);
        tv_usage_time_title.setTypeface(RobotoBold);
        tv_usage_connection_details.setTypeface(RobotoRegular);
        tv_usage_socialmedia_title.setTypeface(RobotoBold);
        tv_usage_appstore_title.setTypeface(RobotoBold);

        // error prone
        SharedPreferences sp_settings = getSharedPreferences("settings_data", 0);
        try {
            String device_created = sp_settings.getString("device_created", "null");
            if (!device_created.equals("null")) {
                long deviceTime = Long.valueOf(device_created);
                long nowTime = System.currentTimeMillis();
                long elapsedTime = nowTime - deviceTime;

                if (nowTime > deviceTime) {
                    if (elapsedTime >= 120_000 && elapsedTime < 3_600_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toMinutes(elapsedTime) + " Minutes Ago");
                    } else if (elapsedTime >= 3_600_000 && elapsedTime < 7_200_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toHours(elapsedTime) + " Hour Ago");
                    } else if (elapsedTime >= 7_200_000 && elapsedTime < 86_400_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toHours(elapsedTime) + " Hours Ago");
                    } else if (elapsedTime >= 86_400_000 && elapsedTime < 172_800_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toDays(elapsedTime) + " Day Ago");
                    } else if (elapsedTime >= 172_800_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toDays(elapsedTime) + " Days Ago");
                    } else if (elapsedTime >= 60_000 && elapsedTime < 120_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toMinutes(elapsedTime) + " Minute ago");
                    } else if (elapsedTime < 60_000) {
                        tv_usage_cu_title.setText("Installed " + TimeUnit.MILLISECONDS.toSeconds(elapsedTime) + " Seconds Ago");
                    }
                }

            }
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", App.device_id);
            params.putString("exception", "UA1" + e.toString());
            mFirebaseAnalytics.logEvent("app_param_error", params);
            tv_usage_cu_title.setText("NA");
        }

        // today
        Date Today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String TODAY = df.format(Today);

        // yesterday
        Calendar Cal1 = Calendar.getInstance();
        Cal1.add(Calendar.DATE, -1);
        String YESTERDAY = df.format(new Date(Cal1.getTimeInMillis()));

        // three days
        Calendar Cal2 = Calendar.getInstance();
        Cal2.add(Calendar.DATE, -2);
        String THREEDAYS = df.format(new Date(Cal2.getTimeInMillis()));

        String WEEK = String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
        String MONTH = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
        String YEAR = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        // get today total usage
        SharedPreferences settings = getSharedPreferences(Data.PREF_USAGE, 0);
        long TODAY_USAGE = settings.getLong(TODAY, 0);
        long YESTERDAY_USAGE = settings.getLong(YESTERDAY, 0);
        long DAYTHREE_USAGE = settings.getLong(THREEDAYS, 0);
        long WEEK_USAGE = settings.getLong(WEEK + YEAR, 0);
        long MONTH_USAGE = settings.getLong(MONTH + YEAR, 0);

        TextView tv_usage_data_today_title = findViewById(R.id.tv_usage_data_today_title);
        TextView tv_usage_data_today_size = findViewById(R.id.tv_usage_data_today_size);
        TextView tv_usage_data_today_used = findViewById(R.id.tv_usage_data_today_used);
        tv_usage_data_today_title.setTypeface(RobotoRegular);
        tv_usage_data_today_size.setTypeface(RobotoLight);
        tv_usage_data_today_used.setTypeface(RobotoMedium);

        if (TODAY_USAGE < 1000) {
            tv_usage_data_today_size.setText("1KB");
        } else if ((TODAY_USAGE >= 1000) && (TODAY_USAGE <= 1000_000)) {
            tv_usage_data_today_size.setText((TODAY_USAGE / 1000) + "KB");
        } else {
            tv_usage_data_today_size.setText((TODAY_USAGE / 1000_000) + "MB");
        }


        TextView tv_usage_data_yesterday_title = findViewById(R.id.tv_usage_data_yesterday_title);
        TextView tv_usage_data_yesterday_size = findViewById(R.id.tv_usage_data_yesterday_size);
        TextView tv_usage_data_yesterday_used = findViewById(R.id.tv_usage_data_yesterday_used);
        tv_usage_data_yesterday_title.setTypeface(RobotoRegular);
        tv_usage_data_yesterday_size.setTypeface(RobotoLight);
        tv_usage_data_yesterday_used.setTypeface(RobotoMedium);

        if (YESTERDAY_USAGE == 0) {
            tv_usage_data_yesterday_size.setText("NA");
        } else if (YESTERDAY_USAGE < 1000) {
            tv_usage_data_yesterday_size.setText("1KB");
        } else if ((YESTERDAY_USAGE >= 1000) && (YESTERDAY_USAGE <= 1000_000)) {
            tv_usage_data_yesterday_size.setText((YESTERDAY_USAGE / 1000) + "KB");
        } else {
            tv_usage_data_yesterday_size.setText((YESTERDAY_USAGE / 1000_000) + "MB");
        }

        TextView tv_usage_data_daythree_title = findViewById(R.id.tv_usage_data_daythree_title);
        TextView tv_usage_data_daythree_size = findViewById(R.id.tv_usage_data_daythree_size);
        TextView tv_usage_data_daythree_used = findViewById(R.id.tv_usage_data_daythree_used);
        tv_usage_data_daythree_title.setTypeface(RobotoRegular);
        tv_usage_data_daythree_size.setTypeface(RobotoLight);
        tv_usage_data_daythree_used.setTypeface(RobotoMedium);
        tv_usage_data_daythree_title.setText(THREEDAYS);

        if (DAYTHREE_USAGE == 0) {
            tv_usage_data_daythree_size.setText("NA");
        } else if (DAYTHREE_USAGE < 1000) {
            tv_usage_data_daythree_size.setText("1KB");
        } else if ((DAYTHREE_USAGE >= 1000) && (DAYTHREE_USAGE <= 1000_000)) {
            tv_usage_data_daythree_size.setText((DAYTHREE_USAGE / 1000) + "KB");
        } else {
            tv_usage_data_daythree_size.setText((DAYTHREE_USAGE / 1000_000) + "MB");
        }

        TextView tv_usage_data_thisweek_title = findViewById(R.id.tv_usage_data_thisweek_title);
        TextView tv_usage_data_thisweek_size = findViewById(R.id.tv_usage_data_thisweek_size);
        TextView tv_usage_data_thisweek_used = findViewById(R.id.tv_usage_data_thisweek_used);
        tv_usage_data_thisweek_title.setTypeface(RobotoRegular);
        tv_usage_data_thisweek_size.setTypeface(RobotoLight);
        tv_usage_data_thisweek_used.setTypeface(RobotoMedium);
        if (WEEK_USAGE == 0) {
            tv_usage_data_thisweek_size.setText("NA");
        } else if (WEEK_USAGE < 1000) {
            tv_usage_data_thisweek_size.setText("1KB");
        } else if ((WEEK_USAGE >= 1000) && (WEEK_USAGE <= 1000_000)) {
            tv_usage_data_thisweek_size.setText((WEEK_USAGE / 1000) + "KB");
        } else {
            tv_usage_data_thisweek_size.setText((WEEK_USAGE / 1000_000) + "MB");
        }

        TextView tv_usage_data_thismonth_title = findViewById(R.id.tv_usage_data_thismonth_title);
        TextView tv_usage_data_thismonth_size = findViewById(R.id.tv_usage_data_thismonth_size);
        TextView tv_usage_data_thismonth_used = findViewById(R.id.tv_usage_data_thismonth_used);
        tv_usage_data_thismonth_title.setTypeface(RobotoRegular);
        tv_usage_data_thismonth_size.setTypeface(RobotoLight);
        tv_usage_data_thismonth_used.setTypeface(RobotoMedium);
        if (MONTH_USAGE == 0) {
            tv_usage_data_thismonth_size.setText("NA");
        } else if (MONTH_USAGE < 1000) {
            tv_usage_data_thismonth_size.setText("1KB");
        } else if ((MONTH_USAGE >= 1000) && (MONTH_USAGE <= 1000_000)) {
            tv_usage_data_thismonth_size.setText((MONTH_USAGE / 1000) + "KB");
        } else {
            tv_usage_data_thismonth_size.setText((MONTH_USAGE / 1000_000) + "MB");
        }

        sp_settings = getSharedPreferences("daily_usage", 0);

        long time_today = sp_settings.getLong(TODAY + "_time", 0);
        long time_yesterday = sp_settings.getLong(YESTERDAY + "_time", 0);
        long time_total = sp_settings.getLong("total_time", 0);


        String TodayTime = String.format(getString(R.string.string_of_two_number), (time_today / (1000 * 60 * 60)) % 24) + ":" +
                String.format(getString(R.string.string_of_two_number), TimeUnit.MILLISECONDS.toMinutes(time_today) % 60) + ":" +
                String.format(getString(R.string.string_of_two_number), (TimeUnit.MILLISECONDS.toSeconds(time_today) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_today))));

        String YesterdayTime = String.format(getString(R.string.string_of_two_number), (time_yesterday / (1000 * 60 * 60)) % 24) + ":" +
                String.format(getString(R.string.string_of_two_number), TimeUnit.MILLISECONDS.toMinutes(time_yesterday) % 60) + ":" +
                String.format(getString(R.string.string_of_two_number), (TimeUnit.MILLISECONDS.toSeconds(time_yesterday) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_yesterday))));


        String TotalTime = String.format(getString(R.string.string_of_two_number), (time_total / (1000 * 60 * 60)) % 24) + ":" +
                String.format(getString(R.string.string_of_two_number), TimeUnit.MILLISECONDS.toMinutes(time_total) % 60) + ":" +
                String.format(getString(R.string.string_of_two_number), (TimeUnit.MILLISECONDS.toSeconds(time_total) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_total))));

        TextView tv_usage_time_today_title = findViewById(R.id.tv_usage_time_today_title);
        TextView tv_usage_time_today_time = findViewById(R.id.tv_usage_time_today_time);
        TextView tv_usage_time_today_metric = findViewById(R.id.tv_usage_time_today_metric);
        TextView tv_usage_time_today_used = findViewById(R.id.tv_usage_time_today_used);
        tv_usage_time_today_title.setTypeface(RobotoRegular);
        tv_usage_time_today_time.setTypeface(RobotoLight);
        tv_usage_time_today_metric.setTypeface(RobotoLight);
        tv_usage_time_today_used.setTypeface(RobotoMedium);
        tv_usage_time_today_time.setText(TodayTime);

        TextView tv_usage_time_yesterday_title = findViewById(R.id.tv_usage_time_yesterday_title);
        TextView tv_usage_time_yesterday_time = findViewById(R.id.tv_usage_time_yesterday_time);
        TextView tv_usage_time_yesterday_metric = findViewById(R.id.tv_usage_time_yesterday_metric);
        TextView tv_usage_time_yesterday_used = findViewById(R.id.tv_usage_time_yesterday_used);
        tv_usage_time_yesterday_title.setTypeface(RobotoRegular);
        tv_usage_time_yesterday_time.setTypeface(RobotoLight);
        tv_usage_time_yesterday_metric.setTypeface(RobotoLight);
        tv_usage_time_yesterday_used.setTypeface(RobotoMedium);
        tv_usage_time_yesterday_time.setText(YesterdayTime);


        TextView tv_usage_time_total_title = findViewById(R.id.tv_usage_time_total_title);
        TextView tv_usage_time_total_time = findViewById(R.id.tv_usage_time_total_time);
        TextView tv_usage_time_total_metric = findViewById(R.id.tv_usage_time_total_metric);
        TextView tv_usage_time_total_used = findViewById(R.id.tv_usage_time_total_used);
        tv_usage_time_total_title.setTypeface(RobotoRegular);
        tv_usage_time_total_time.setTypeface(RobotoLight);
        tv_usage_time_total_metric.setTypeface(RobotoLight);
        tv_usage_time_total_used.setTypeface(RobotoMedium);
        tv_usage_time_total_time.setText(TotalTime);

        long connections_today = sp_settings.getLong(TODAY + "_connections", 0);
        long connections_yesterday = sp_settings.getLong(YESTERDAY + "_connections", 0);
        long connections_total = sp_settings.getLong("total_connections", 0);

        TextView tv_usage_connection_today_title = findViewById(R.id.tv_usage_connection_today_title);
        TextView tv_usage_connection_today_size = findViewById(R.id.tv_usage_connection_today_size);
        TextView tv_usage_connection_today_used = findViewById(R.id.tv_usage_connection_today_used);
        tv_usage_connection_today_title.setTypeface(RobotoRegular);
        tv_usage_connection_today_size.setTypeface(RobotoLight);
        tv_usage_connection_today_used.setTypeface(RobotoMedium);
        tv_usage_connection_today_size.setText(String.valueOf(connections_today));

        TextView tv_usage_connection_yesterday_title = findViewById(R.id.tv_usage_connection_yesterday_title);
        TextView tv_usage_connection_yesterday_size = findViewById(R.id.tv_usage_connection_yesterday_size);
        TextView tv_usage_connection_yesterday_used = findViewById(R.id.tv_usage_connection_yesterday_used);
        tv_usage_connection_yesterday_title.setTypeface(RobotoRegular);
        tv_usage_connection_yesterday_size.setTypeface(RobotoLight);
        tv_usage_connection_yesterday_used.setTypeface(RobotoMedium);
        tv_usage_connection_yesterday_size.setText(String.valueOf(connections_yesterday));


        TextView tv_usage_connection_total_title = findViewById(R.id.tv_usage_connection_total_title);
        TextView tv_usage_connection_total_size = findViewById(R.id.tv_usage_connection_total_size);
        TextView tv_usage_connection_total_used = findViewById(R.id.tv_usage_connection_total_used);
        tv_usage_connection_total_title.setTypeface(RobotoRegular);
        tv_usage_connection_total_size.setTypeface(RobotoLight);
        tv_usage_connection_total_used.setTypeface(RobotoMedium);
        tv_usage_connection_total_size.setText(String.valueOf(connections_total));


        LinearLayout ll_about_forward = findViewById(R.id.ll_about_forward);
        ll_about_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    finish();
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA2" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_facebook = findViewById(R.id.iv_about_facebook);
        iv_about_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "facebook");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://web.facebook.com/OML-100989851252068/"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA3" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_vk = findViewById(R.id.iv_about_vk);
        iv_about_vk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "vk");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://vk.com/public185007005"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA4" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_youtube = findViewById(R.id.iv_about_youtube);
        iv_about_youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "youtube");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.youtube.com/channel/UCnCrXRM8U75iKvSpO6yXFKQ"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA5" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_twitter = findViewById(R.id.iv_about_twitter);
        iv_about_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "twitter");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://twitter.com/OML69079868"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA6" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_instagram = findViewById(R.id.iv_about_instagram);
        iv_about_instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "instagram");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.instagram.com/gayankr/"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA7" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });


        ImageView iv_about_playstore = findViewById(R.id.iv_about_playstore);
        iv_about_playstore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "play-store");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.buzz.vpn"));
                    startActivity(intent);
                } catch (ActivityNotFoundException activityNotFound) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.buzz.vpn")));
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA8" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_amazon = findViewById(R.id.iv_about_amazon);
        iv_about_amazon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "amazon");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.amazon.com/Buzz-VPN-Best-Free-Unlimited/dp/B07T3X677T/ref=BuzzApp"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA9" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);

                }
            }
        });

        // https://buzz-vpn-fast-free-unlimited-secure-vpn-proxy.en.uptodown.com/android
        ImageView iv_about_uptodown = findViewById(R.id.iv_about_uptodown);
        iv_about_uptodown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "uptodown");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://buzz-vpn-fast-free-unlimited-secure-vpn-proxy.en.uptodown.com/android"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA10" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        ImageView iv_about_aptoid = findViewById(R.id.iv_about_aptoid);
        iv_about_aptoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "aptoid");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    //interstitialBuilder.show(UsageActivity.this);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://buzz-vpn.en.aptoide.com/"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA11" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });


        ImageView iv_about_yandex = findViewById(R.id.iv_about_yandex);
        iv_about_yandex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "yandex");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://store.yandex.com/"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA12" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });


        LinearLayout linearLayoutFAQ = findViewById(R.id.linearLayoutFAQ);
        TextView tv_usage_faq_title = findViewById(R.id.tv_usage_faq_title);
        TextView tv_usage_faq_description = findViewById(R.id.tv_usage_faq_description);
        tv_usage_faq_title.setTypeface(RobotoMedium);
        tv_usage_faq_description.setTypeface(RobotoRegular);
        linearLayoutFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "faq");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent Servers = new Intent(UsageActivity.this, FAQActivity.class);
                    startActivity(Servers);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA13" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }

        });

        LinearLayout linearLayoutBattery = findViewById(R.id.linearLayoutBattery);
        TextView tv_usage_battery_title = findViewById(R.id.tv_usage_battery_title);
        TextView tv_usage_battery_description = findViewById(R.id.tv_usage_battery_description);
        tv_usage_battery_title.setTypeface(RobotoMedium);
        tv_usage_battery_description.setTypeface(RobotoRegular);
        try {
            if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                linearLayoutBattery.setVisibility(View.VISIBLE);
            } else {
                linearLayoutBattery.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Bundle params = new Bundle();
            params.putString("device_id", App.device_id);
            params.putString("exception", "UA14" + e.toString());
            mFirebaseAnalytics.logEvent("app_param_error", params);
        }

        linearLayoutBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UsageActivity.this);
                        builder.setTitle("Let app always run in background?").setMessage("Allowing Buzz VPN to always run in the background app may can reduce memory usage")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            Intent intent = new Intent();
                                            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            Bundle params = new Bundle();
                                            params.putString("device_id", App.device_id);
                                            params.putString("exception", "UA15" + e.toString());
                                            mFirebaseAnalytics.logEvent("app_param_error", params);
                                        }
                                    }
                                }).create().show();
                        Bundle params = new Bundle();
                        params.putString("device_id", App.device_id);
                        params.putString("click", "amazon");
                        mFirebaseAnalytics.logEvent("app_param_click", params);
                    }
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA16" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        LinearLayout linearLayoutPrivacyPolicy = findViewById(R.id.linearLayoutPrivacyPolicy);
        TextView tv_usage_privacy_title = findViewById(R.id.tv_usage_privacy_title);
        TextView tv_usage_privacy_decription = findViewById(R.id.tv_usage_privacy_decription);
        tv_usage_privacy_title.setTypeface(RobotoMedium);
        tv_usage_privacy_decription.setTypeface(RobotoRegular);
        linearLayoutPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "privacy");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://gayanvoice.github.io/oml/buzz/privacypolicy.html"));
                    startActivity(intent);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA17" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        LinearLayout ll_open_contact_dialog = findViewById(R.id.linearLayoutContact);
        TextView tv_usage_contact_title = findViewById(R.id.tv_usage_contact_title);
        TextView tv_usage_contact_description = findViewById(R.id.tv_usage_contact_description);
        TextView tv_usage_connections_title = findViewById(R.id.tv_usage_connections_title);

        tv_usage_connections_title.setTypeface(RobotoBold);
        tv_usage_contact_title.setTypeface(RobotoMedium);
        tv_usage_contact_description.setTypeface(RobotoRegular);
        tv_usage_contact_description.setTypeface(RobotoRegular);
        ll_open_contact_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "contact");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent Servers = new Intent(UsageActivity.this, ContactActivity.class);
                    startActivity(Servers);
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA18" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        LinearLayout ll_usage_share = findViewById(R.id.linearLayoutShare);
        tv_usage_share_title.setTypeface(RobotoBold);
        tv_usage_share_description.setTypeface(RobotoRegular);
        ll_usage_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("click", "share");
                mFirebaseAnalytics.logEvent("app_param_click", params);

                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Wow! Change to dark mode, check VPN data usage, +10 countries, and it's only 5MB! Download the App NOW! From Google Play http://bit.ly/gotogoogleplay | Get the APK from UpToDown http://bit.ly/gotouptodown Aptoid http://bit.ly/gotoaptoide");
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } catch (Exception e) {
                    params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA19" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }
        });

        View viewUsageDark_1 = findViewById(R.id.viewUsageDark_1);
        View viewUsageDark_2 = findViewById(R.id.viewUsageDark_2);
        View viewUsageDark_3 = findViewById(R.id.viewUsageDark_3);
        View viewUsageDark_4 = findViewById(R.id.viewUsageDark_4);
        View viewUsageDark_5 = findViewById(R.id.viewUsageDark_5);
        View viewUsageDark_6 = findViewById(R.id.viewUsageDark_6);
        View viewUsageDark_7 = findViewById(R.id.viewUsageDark_7);
        View viewUsageDark_8 = findViewById(R.id.viewUsageDark_8);
        View viewUsageDark_9 = findViewById(R.id.viewUsageDark_9);
        View viewUsageDark_10 = findViewById(R.id.viewUsageDark_10);

        View viewUsageLight_1 = findViewById(R.id.viewUsageLight_1);
        View viewUsageLight_2 = findViewById(R.id.viewUsageLight_2);
        View viewUsageLight_3 = findViewById(R.id.viewUsageLight_3);
        View viewUsageLight_4 = findViewById(R.id.viewUsageLight_4);
        View viewUsageLight_5 = findViewById(R.id.viewUsageLight_5);
        View viewUsageLight_6 = findViewById(R.id.viewUsageLight_6);
        View viewUsageLight_7 = findViewById(R.id.viewUsageLight_7);
        View viewUsageLight_8 = findViewById(R.id.viewUsageLight_8);
        View viewUsageLight_9 = findViewById(R.id.viewUsageLight_9);
        View viewUsageLight_10 = findViewById(R.id.viewUsageLight_10);

        LinearLayout linearLayoutUsage = findViewById(R.id.linearLayoutUsage);
        TextView tv_usage_dark_mode_title = findViewById(R.id.tv_usage_dark_mode_title);

        Switch switch_usage_dark_mode = findViewById(R.id.switch_usage_dark_mode);
        switch_usage_dark_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    if (isChecked) {
                        try {
                            Editor.putString("dark_mode", "true");
                        } catch (Exception e) {
                            Editor.putString("dark_mode", "false");
                        }
                    } else {
                        Editor.putString("dark_mode", "false");
                    }
                    Editor.apply();
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "UA20" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }


        });

        tv_usage_dark_mode_title.setTypeface(RobotoMedium);
        switch_usage_dark_mode.setTypeface(RobotoRegular);

        SharedPreferences SettingsDetails = getSharedPreferences("settings_data", 0);
        String DarkMode = SettingsDetails.getString("dark_mode", "false");
        if (DarkMode.equals("true")) {
            linearLayoutUsage.setBackgroundColor(getResources().getColor(R.color.colorDarkBackground));
            tv_usage_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_data_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_time_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_connection_details.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_socialmedia_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_appstore_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_description.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_dark_mode_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            switch_usage_dark_mode.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_battery_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_battery_description.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_privacy_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_privacy_decription.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_contact_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_contact_description.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_share_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_share_description.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_cu_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_cu_version.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_app_name.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_app_copyright.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_connections_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            iv_go_forward.setImageResource(R.drawable.ic_go_forward_white);

            try {
                if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                    viewUsageLight_6.setVisibility(View.GONE);
                    viewUsageDark_6.setVisibility(View.VISIBLE);
                } else {
                    viewUsageLight_6.setVisibility(View.GONE);
                    viewUsageDark_6.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "UA21" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            viewUsageLight_1.setVisibility(View.GONE);
            viewUsageLight_2.setVisibility(View.GONE);
            viewUsageLight_3.setVisibility(View.GONE);
            viewUsageLight_4.setVisibility(View.GONE);
            viewUsageLight_5.setVisibility(View.GONE);
            viewUsageLight_7.setVisibility(View.GONE);
            viewUsageLight_8.setVisibility(View.GONE);
            viewUsageLight_9.setVisibility(View.GONE);
            viewUsageLight_10.setVisibility(View.GONE);

            viewUsageDark_1.setVisibility(View.VISIBLE);
            viewUsageDark_2.setVisibility(View.VISIBLE);
            viewUsageDark_3.setVisibility(View.VISIBLE);
            viewUsageDark_4.setVisibility(View.VISIBLE);
            viewUsageDark_5.setVisibility(View.VISIBLE);
            viewUsageDark_7.setVisibility(View.VISIBLE);
            viewUsageDark_8.setVisibility(View.VISIBLE);
            viewUsageDark_9.setVisibility(View.VISIBLE);
            viewUsageDark_10.setVisibility(View.VISIBLE);

        } else {
            linearLayoutUsage.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
            tv_usage_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_data_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_time_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_connection_details.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_socialmedia_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_appstore_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_description.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_dark_mode_title.setTextColor(getResources().getColor(R.color.colorLightText));
            switch_usage_dark_mode.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_battery_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_battery_description.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_privacy_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_privacy_decription.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_contact_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_contact_description.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_share_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_share_description.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_cu_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_cu_version.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_connections_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_app_name.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_app_copyright.setTextColor(getResources().getColor(R.color.colorLightText));
            iv_go_forward.setImageResource(R.drawable.ic_go_forward);

            try {
                if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                    viewUsageDark_6.setVisibility(View.GONE);
                    viewUsageLight_6.setVisibility(View.VISIBLE);
                } else {
                    viewUsageDark_6.setVisibility(View.GONE);
                    viewUsageLight_6.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "UA22" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            viewUsageDark_1.setVisibility(View.GONE);
            viewUsageDark_2.setVisibility(View.GONE);
            viewUsageDark_3.setVisibility(View.GONE);
            viewUsageDark_4.setVisibility(View.GONE);
            viewUsageDark_5.setVisibility(View.GONE);
            viewUsageDark_6.setVisibility(View.GONE);
            viewUsageDark_7.setVisibility(View.GONE);
            viewUsageDark_8.setVisibility(View.GONE);
            viewUsageDark_9.setVisibility(View.GONE);
            viewUsageDark_10.setVisibility(View.GONE);

            viewUsageLight_1.setVisibility(View.VISIBLE);
            viewUsageLight_2.setVisibility(View.VISIBLE);
            viewUsageLight_3.setVisibility(View.VISIBLE);
            viewUsageLight_4.setVisibility(View.VISIBLE);
            viewUsageLight_5.setVisibility(View.VISIBLE);
            viewUsageLight_6.setVisibility(View.VISIBLE);
            viewUsageLight_7.setVisibility(View.VISIBLE);
            viewUsageLight_8.setVisibility(View.VISIBLE);
            viewUsageLight_9.setVisibility(View.VISIBLE);
            viewUsageLight_10.setVisibility(View.VISIBLE);
        }

        if (DarkMode.equals("true")) {
            switch_usage_dark_mode.setChecked(true);
        } else {
            switch_usage_dark_mode.setChecked(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
