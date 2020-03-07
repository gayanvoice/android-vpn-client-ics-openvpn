package com.buzz.vpn;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import de.blinkt.openvpn.core.App;


public class WelcomeActivity extends AppCompatActivity {
    TextView tv_welcome_status, tv_welcome_app;
    String StringGetAppURL, StringGetConnectionURL, AppDetails, FileDetails;
    SharedPreferences SharedAppDetails;
    TextView tv_welcome_title, tv_welcome_description, tv_welcome_size, tv_welcome_version;

    int Random;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        StringGetAppURL = "https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/appdetails.json";
        StringGetConnectionURL = "https://raw.githubusercontent.com/gayanvoice/android-vpn-client-ics-openvpn/images/filedetails.json";
        //StringGetConnectionURL = "https://gayankuruppu.github.io/buzz/connection.html";

        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface RobotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface RobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

        tv_welcome_status = findViewById(R.id.tv_welcome_status);
        tv_welcome_app = findViewById(R.id.tv_welcome_app);
        tv_welcome_title = findViewById(R.id.tv_welcome_title);
        tv_welcome_description = findViewById(R.id.tv_welcome_description);
        tv_welcome_size = findViewById(R.id.tv_welcome_size);
        tv_welcome_version = findViewById(R.id.tv_welcome_version);

        tv_welcome_title.setTypeface(RobotoMedium);
        tv_welcome_description.setTypeface(RobotoRegular);
        tv_welcome_size.setTypeface(RobotoMedium);
        tv_welcome_version.setTypeface(RobotoMedium);


        startAnimation(WelcomeActivity.this, R.id.ll_welcome_loading, R.anim.slide_up_800, true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(WelcomeActivity.this, R.id.ll_welcome_details, R.anim.slide_up_800, true);
            }
        }, 1000);


        tv_welcome_status.setTypeface(RobotoMedium);
        tv_welcome_app.setTypeface(RobotoBold);

        Button btn_welcome_update = findViewById(R.id.btn_welcome_update);
        Button btn_welcome_later = findViewById(R.id.btn_welcome_later);

        btn_welcome_update.setTypeface(RobotoMedium);
        btn_welcome_later.setTypeface(RobotoMedium);

        btn_welcome_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gayanvoice/android-vpn-client-ics-openvpn")));
                    /*
                    The following lines of code load the PlayStore

                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("click", "play");
                    mFirebaseAnalytics.logEvent("app_param_click", params);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.buzz.vpn"));
                    startActivity(intent);
                    */
                } catch (ActivityNotFoundException activityNotFound) {
                    // startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.buzz.vpn")));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gayanvoice/android-vpn-client-ics-openvpn")));

                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA1" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            }

        });

        btn_welcome_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);

            }

        });

        if (!Data.isConnectionDetails) {
            if (!Data.isAppDetails) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getAppDetails();
                    }
                }, 2000);
            }
        }

    }

    void getAppDetails() {
        tv_welcome_status.setText("GETTING APP DETAILS");
        RequestQueue queue = Volley.newRequestQueue(WelcomeActivity.this);
        queue.getCache().clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetAppURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {
                        Log.e("Response", Response);
                        AppDetails = Response;
                        Data.isAppDetails = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "WA2" + error.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);

                Data.isAppDetails = false;
            }
        });

        queue.add(stringRequest);
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {
                if (Data.isAppDetails) {
                    getFileDetails();
                } else {
                    tv_welcome_status.setText("CONNECTION INTERRUPTED");
                }
            }
        });
    }

    void getFileDetails() {
        tv_welcome_status.setText("GETTING CONNECTION DETAILS");
        RequestQueue queue = Volley.newRequestQueue(WelcomeActivity.this);
        queue.getCache().clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetConnectionURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {
                        FileDetails = Response;
                        Data.isConnectionDetails = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "WA3" + error.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);

                Data.isConnectionDetails = false;
            }
        });
        queue.add(stringRequest);
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {

                final int min = 0;
                final int max = 4;
                Random = new Random().nextInt((max - min) + 1) + min;

                String Ads = "NULL", cuVersion = "NULL", upVersion = "NULL", upTitle = "NULL", upDescription = "NULL", upSize = "NULL";
                String ID = "NULL", FileID = "NULL", File = "NULL", City = "NULL", Country = "NULL", Image = "NULL",
                        IP = "NULL", Active = "NULL", Signal = "NULL";
                String BlockedApps = "NULL";

                try {
                    JSONObject jsonResponse = new JSONObject(AppDetails);
                    Ads = jsonResponse.getString("ads");
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA4" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

                try {
                    JSONObject jsonResponse = new JSONObject(AppDetails);
                    JSONArray jsonArray = jsonResponse.getJSONArray("update");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    upVersion = jsonObject.getString("version");
                    upTitle = jsonObject.getString("title");
                    upDescription = jsonObject.getString("description");
                    upSize = jsonObject.getString("size");
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA5" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

                try {
                    JSONObject json_response = new JSONObject(AppDetails);
                    JSONArray jsonArray = json_response.getJSONArray("free");
                    JSONObject json_object = jsonArray.getJSONObject(Random);
                    ID = json_object.getString("id");
                    FileID = json_object.getString("file");
                    City = json_object.getString("city");
                    Country = json_object.getString("country");
                    Image = json_object.getString("image");
                    IP = json_object.getString("ip");
                    Active = json_object.getString("active");
                    Signal = json_object.getString("signal");
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA5" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

                try {
                    JSONObject json_response = new JSONObject(FileDetails);
                    JSONArray jsonArray = json_response.getJSONArray("ovpn_file");
                    JSONObject json_object = jsonArray.getJSONObject(Integer.valueOf(FileID));
                    FileID = json_object.getString("id");
                    File = json_object.getString("file");
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA6" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }


                // save details
                EncryptData En = new EncryptData();
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    cuVersion = pInfo.versionName;
                    if (cuVersion.isEmpty()) {
                        cuVersion = "0.0.0";
                    }

                    SharedAppDetails = getSharedPreferences("app_details", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("ads", Ads);
                    Editor.putString("up_title", upTitle);
                    Editor.putString("up_description", upDescription);
                    Editor.putString("up_size", upSize);
                    Editor.putString("up_version", upVersion);
                    Editor.putString("cu_version", cuVersion);
                    Editor.apply();
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA7" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

                try {
                    SharedAppDetails = getSharedPreferences("connection_data", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("id", ID);
                    Editor.putString("file_id", FileID);
                    Editor.putString("file", En.encrypt(File));
                    Editor.putString("city", City);
                    Editor.putString("country", Country);
                    Editor.putString("image", Image);
                    Editor.putString("ip", IP);
                    Editor.putString("active", Active);
                    Editor.putString("signal", Signal);
                    Editor.apply();
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA8" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

                try {
                    SharedAppDetails = getSharedPreferences("app_values", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("app_details", En.encrypt(AppDetails));
                    Editor.putString("file_details", En.encrypt(FileDetails));
                    Editor.apply();
                } catch (Exception e) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "WA9" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

                tv_welcome_title.setText(upTitle);
                tv_welcome_description.setText(upDescription);
                tv_welcome_size.setText(upSize);
                tv_welcome_version.setText(upVersion);

                if (Data.isConnectionDetails) {
                    if (cuVersion.equals(upVersion)) {
                        finish();
                        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                    } else {
                        startAnimation(WelcomeActivity.this, R.id.ll_welcome_loading, R.anim.fade_out_500, false);
                        Handler handlerData = new Handler();
                        handlerData.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startAnimation(WelcomeActivity.this, R.id.ll_update_details, R.anim.slide_up_800, true);
                            }
                        }, 1000);
                    }
                } else {
                    tv_welcome_status.setText("CONNECTION INTERRUPTED");
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
