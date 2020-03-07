package com.buzz.vpn;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import de.blinkt.openvpn.core.App;


public class ReviewActivity extends Activity {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Typeface RobotoBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

        LinearLayout ll_about_back = findViewById(R.id.ll_about_back);
        LottieAnimationView la_review = findViewById(R.id.la_review);
        TextView tv_review_title = findViewById(R.id.tv_review_title);
        TextView tv_review_sup = findViewById(R.id.tv_review_sup);
        TextView tv_review_sub = findViewById(R.id.tv_review_sub);
        Button btn_review_submit = findViewById(R.id.btn_review_submit);

        startAnimation(ReviewActivity.this, R.id.la_review, R.anim.slide_up_800, true);
        startAnimation(ReviewActivity.this, R.id.ll_about_back, R.anim.anim_slide_down, true);
        startAnimation(ReviewActivity.this, R.id.tv_review_title, R.anim.slide_up_800, true);
        startAnimation(ReviewActivity.this, R.id.tv_review_sup, R.anim.slide_up_800, true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(ReviewActivity.this, R.id.btn_review_submit, R.anim.slide_up_800, true);
            }
        }, 500);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(ReviewActivity.this, R.id.tv_review_sub, R.anim.slide_up_800, true);
            }
        }, 1000);

        tv_review_title.setTypeface(RobotoBold);
        tv_review_sup.setTypeface(RobotoBold);
        tv_review_sub.setTypeface(RobotoBold);
        btn_review_submit.setTypeface(RobotoBold);

        ll_about_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            }
        });

        la_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
                SharedPreferences.Editor Editor = SharedAppDetails.edit();
                Editor.putString("rate", "true");
                Editor.apply();

                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("click", "review-stars");
                mFirebaseAnalytics.logEvent("app_param_click", params);


                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.buzz.vpn"));
                    startActivity(intent);
                } catch (ActivityNotFoundException activityNotFound) {
                    params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "RA1" + activityNotFound.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.buzz.vpn")));
                } catch (Exception e) {
                    params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "RA2" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }

            }

        });

        btn_review_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
                SharedPreferences.Editor Editor = SharedAppDetails.edit();
                Editor.putString("rate", "true");
                Editor.apply();

                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("click", "review-button");
                mFirebaseAnalytics.logEvent("app_param_click", params);

                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.buzz.vpn"));
                    startActivity(intent);
                } catch (ActivityNotFoundException activityNotFound) {
                    params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "RA2" + activityNotFound.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.buzz.vpn")));
                } catch (Exception e) {
                    params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "RA3" + e.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
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
