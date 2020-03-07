package com.buzz.vpn;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class FAQActivity extends Activity {

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        TextView tv_usage_faq_title = findViewById(R.id.tv_usage_faq_title);
        TextView tv_usage_faq_process_1 = findViewById(R.id.tv_usage_faq_process_1);
        TextView tv_usage_faq_process_2 = findViewById(R.id.tv_usage_faq_process_2);
        TextView tv_usage_faq_q1_1 = findViewById(R.id.tv_usage_faq_q1_1);
        TextView tv_usage_faq_q1_2 = findViewById(R.id.tv_usage_faq_q1_2);

        TextView tv_usage_faq_q2_1 = findViewById(R.id.tv_usage_faq_q2_1);
        TextView tv_usage_faq_q2_2 = findViewById(R.id.tv_usage_faq_q2_2);

        TextView tv_usage_faq_q3_1 = findViewById(R.id.tv_usage_faq_q3_1);
        TextView tv_usage_faq_q3_2 = findViewById(R.id.tv_usage_faq_q3_2);

        TextView tv_usage_faq_q4_1 = findViewById(R.id.tv_usage_faq_q4_1);
        TextView tv_usage_faq_q4_2 = findViewById(R.id.tv_usage_faq_q4_2);

        TextView tv_usage_faq_q5_1 = findViewById(R.id.tv_usage_faq_q5_1);
        TextView tv_usage_faq_q5_2 = findViewById(R.id.tv_usage_faq_q5_2);
        TextView tv_usage_faq_q5_3 = findViewById(R.id.tv_usage_faq_q5_3);
        TextView tv_usage_faq_q5_4 = findViewById(R.id.tv_usage_faq_q5_4);

        Typeface RobotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");

        tv_usage_faq_title.setTypeface(RobotoMedium);
        tv_usage_faq_process_1.setTypeface(RobotoMedium);
        tv_usage_faq_process_2.setTypeface(RobotoRegular);

        tv_usage_faq_q1_1.setTypeface(RobotoMedium);
        tv_usage_faq_q1_2.setTypeface(RobotoRegular);

        tv_usage_faq_q2_1.setTypeface(RobotoMedium);
        tv_usage_faq_q2_2.setTypeface(RobotoRegular);

        tv_usage_faq_q3_1.setTypeface(RobotoMedium);
        tv_usage_faq_q3_2.setTypeface(RobotoRegular);

        tv_usage_faq_q4_1.setTypeface(RobotoMedium);
        tv_usage_faq_q4_2.setTypeface(RobotoRegular);

        tv_usage_faq_q5_1.setTypeface(RobotoMedium);
        tv_usage_faq_q5_2.setTypeface(RobotoMedium);
        tv_usage_faq_q5_3.setTypeface(RobotoRegular);
        tv_usage_faq_q5_4.setTypeface(RobotoRegular);

        ImageView iv_faq_goback = findViewById(R.id.iv_faq_goback);
        LinearLayout ll_faq_go_back = findViewById(R.id.ll_faq_go_back);
        ll_faq_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            }
        });

        ConstraintLayout constLayoutFAQMain = findViewById(R.id.constLayoutFAQMain);
        LinearLayout linearLayoutFAQMain = findViewById(R.id.linearLayoutFAQMain);
        SharedPreferences SettingsDetails = getSharedPreferences("settings_data", 0);
        String DarkMode = SettingsDetails.getString("dark_mode", "false");
        if (DarkMode.equals("true")) {
            constLayoutFAQMain.setBackgroundColor(getResources().getColor(R.color.colorDarkBackground));
            linearLayoutFAQMain.setBackgroundColor(getResources().getColor(R.color.colorDarkBackground));
            tv_usage_faq_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_process_1.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_process_2.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q1_1.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q1_2.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q2_1.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q2_2.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q3_1.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q3_2.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q4_1.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q4_2.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q5_1.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q5_2.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q5_3.setTextColor(getResources().getColor(R.color.colorDarkText));
            tv_usage_faq_q5_4.setTextColor(getResources().getColor(R.color.colorDarkText));
            iv_faq_goback.setImageResource(R.drawable.ic_go_back_white);
        } else {
            constLayoutFAQMain.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
            linearLayoutFAQMain.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
            tv_usage_faq_title.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_process_1.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_process_2.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q1_1.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q1_2.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q2_1.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q2_2.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q3_1.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q3_2.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q4_1.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q4_2.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q5_1.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q5_2.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q5_3.setTextColor(getResources().getColor(R.color.colorLightText));
            tv_usage_faq_q5_4.setTextColor(getResources().getColor(R.color.colorLightText));
            iv_faq_goback.setImageResource(R.drawable.ic_go_back);
        }


    }

}
