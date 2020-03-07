package com.buzz.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.ProfileManager;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class ServerActivity extends Activity {
    ListView listView_light, listView_dark;
    ProfileManager pm;
    ImageView iv_server_refresh;
    String AppDetails = "NULL", FileDetails = "NULL";

    String[][] ServerArray = new String[40][8];
    String[][] FileArray = new String[40][2];

    String DarkMode = "false";

    private FirebaseAnalytics mFirebaseAnalytics;

    // 100
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        pm = ProfileManager.getInstance(ServerActivity.this);

        iv_server_refresh = findViewById(R.id.iv_server_refresh);

        EncryptData En = new EncryptData();
        SharedPreferences AppValues = getSharedPreferences("app_values", 0);
        String AppDetails = En.decrypt(AppValues.getString("app_details", NULL));

        if (AppDetails.isEmpty()) {
            getConnectionString getConnectionString = new getConnectionString();
            getConnectionString.GetAppDetails();
        } else {
            ServersList Servers = new ServersList();
            Servers.Load();
        }

        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        TextView tv_servers_title = findViewById(R.id.tv_servers_title);
        tv_servers_title.setTypeface(RobotoMedium);

        LinearLayout ll_server_back = findViewById(R.id.ll_server_back);
        ll_server_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            }
        });

        LinearLayout ll_server_retry = findViewById(R.id.ll_server_refresh);
        ll_server_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getConnectionString getConnectionString = new getConnectionString();
                getConnectionString.GetAppDetails();
            }
        });


        LinearLayout linearLayoutServers = findViewById(R.id.constraintLayoutServers);
        ImageView iv_servers_go_back = findViewById(R.id.iv_servers_go_back);
        SharedPreferences SettingsDetails = getSharedPreferences("settings_data", 0);
        DarkMode = SettingsDetails.getString("dark_mode", "false");
        if (DarkMode.equals("true")) {
            linearLayoutServers.setBackgroundColor(getResources().getColor(R.color.colorDarkBackground));
            tv_servers_title.setTextColor(getResources().getColor(R.color.colorDarkText));
            iv_servers_go_back.setImageResource(R.drawable.ic_go_back_white);
        } else {
            linearLayoutServers.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
            tv_servers_title.setTextColor(getResources().getColor(R.color.colorLightText));
            iv_servers_go_back.setImageResource(R.drawable.ic_go_back);
        }

    }

    private class getConnectionString {
        private void GetAppDetails() {
            iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_process));
            RequestQueue queue = Volley.newRequestQueue(ServerActivity.this);
            queue.getCache().clear();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://raw.githubusercontent.com/gayanvoice/gayankuruppu.github.io/source-json/appdetails.json",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String Response) {
                            AppDetails = Response;
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "SA1" + error.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            });
            queue.add(stringRequest);
            queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
                @Override
                public void onRequestFinished(Request<String> request) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_cloud));
                            GetFileDetails();
                        }
                    }, 2000);
                    iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_cloud));
                    GetFileDetails();
                }
            });
        }

        private void GetFileDetails() {
            iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_process));
            RequestQueue queue = Volley.newRequestQueue(ServerActivity.this);
            queue.getCache().clear();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://raw.githubusercontent.com/gayanvoice/gayankuruppu.github.io/source-json/filedetails.json",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String Response) {
                            FileDetails = Response;
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Bundle params = new Bundle();
                    params.putString("device_id", App.device_id);
                    params.putString("exception", "SA2" + error.toString());
                    mFirebaseAnalytics.logEvent("app_param_error", params);
                }
            });
            queue.add(stringRequest);
            queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
                @Override
                public void onRequestFinished(Request<String> request) {
                    EncryptData En = new EncryptData();
                    try {
                        SharedPreferences SharedAppDetails = getSharedPreferences("app_values", 0);
                        SharedPreferences.Editor Editor = SharedAppDetails.edit();
                        Editor.putString("app_details", En.encrypt(AppDetails));
                        Editor.putString("file_details", En.encrypt(FileDetails));
                        Editor.apply();
                    } catch (Exception e) {
                        Bundle params = new Bundle();
                        params.putString("device_id", App.device_id);
                        params.putString("exception", "SA3" + e.toString());
                        mFirebaseAnalytics.logEvent("app_param_error", params);
                    }

                    iv_server_refresh.setBackground(getDrawable(R.drawable.ic_servers_cloud));
                    ServersList Servers = new ServersList();
                    Servers.Load();
                }
            });
        }
    }

    class ServersList {
        private CategoryArray adapter;

        ServersList() {

        }

        void Load() {
            EncryptData En = new EncryptData();
            SharedPreferences ConnectionDetails = getSharedPreferences("app_values", 0);
            AppDetails = En.decrypt(ConnectionDetails.getString("app_details", NULL));
            FileDetails = En.decrypt(ConnectionDetails.getString("file_details", NULL));
            int NumServers = 0;
            try {
                JSONObject json_response = new JSONObject(AppDetails);
                JSONArray jsonArray = json_response.getJSONArray("free");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_object = jsonArray.getJSONObject(i);
                    ServerArray[i][0] = json_object.getString("id");
                    ServerArray[i][1] = json_object.getString("file");
                    ServerArray[i][2] = json_object.getString("city");
                    ServerArray[i][3] = json_object.getString("country");
                    ServerArray[i][4] = json_object.getString("image");
                    ServerArray[i][5] = json_object.getString("ip");
                    ServerArray[i][6] = json_object.getString("active");
                    ServerArray[i][7] = json_object.getString("signal");
                    NumServers = NumServers + 1;
                }

            } catch (JSONException e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "SA4" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            try {
                JSONObject jsonResponse = new JSONObject(FileDetails);
                JSONArray jsonArray = jsonResponse.getJSONArray("ovpn_file");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    FileArray[i][0] = jsonObject.getString("id");
                    FileArray[i][1] = jsonObject.getString("file");
                }

            } catch (JSONException e) {
                Bundle params = new Bundle();
                params.putString("device_id", App.device_id);
                params.putString("exception", "SA5" + e.toString());
                mFirebaseAnalytics.logEvent("app_param_error", params);
            }

            List<Server> ServerList = new ArrayList<>();
            listView_light = findViewById(R.id.ls_servers_list_light);
            listView_dark = findViewById(R.id.ls_servers_list_dark);

            for (int x = 0; x < NumServers; x++) {
                Server Server = new Server();
                Server.SetID(ServerArray[x][0]);
                Server.SetFileID(ServerArray[x][1]);
                Server.SetCity(ServerArray[x][2]);
                Server.SetCountry(ServerArray[x][3]);
                Server.SetImage(ServerArray[x][4]);
                Server.SetIP(ServerArray[x][5]);
                Server.SetActive(ServerArray[x][6]);
                Server.SetSignal(ServerArray[x][7]);
                ServerList.add(Server);
            }

            SharedPreferences SettingsDetails = getSharedPreferences("settings_data", 0);
            String DarkMode = SettingsDetails.getString("dark_mode", "false");
            adapter = new CategoryArray(ServerList, ServerActivity.this);
            if (DarkMode.equals("true")) {
                listView_dark.setAdapter(adapter);
                listView_dark.setVisibility(View.VISIBLE);
                listView_light.setVisibility(View.GONE);
            } else {
                listView_light.setAdapter(adapter);
                listView_light.setVisibility(View.VISIBLE);
                listView_dark.setVisibility(View.GONE);
            }

        }
    }

    public class CategoryArray extends ArrayAdapter<Server> {

        private List<Server> dataSet;
        TextView tv_country;


        private CategoryArray(List<Server> dataSet, Context mContext) {
            super(mContext, R.layout.server_list_item, dataSet);
            this.dataSet = dataSet;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.server_list_item, null);
            }

            final Server Server = dataSet.get(position);

            if (Server != null) {
                tv_country = v.findViewById(R.id.tv_country);
                ImageView iv_flag = v.findViewById(R.id.iv_flag);
                ImageView iv_signal_strength = v.findViewById(R.id.iv_signal_strength);
                final LinearLayout ll_item = v.findViewById(R.id.ll_item);

                Typeface RobotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

                tv_country.setText(Server.GetCity());
                tv_country.setTypeface(RobotoRegular);

                if (DarkMode.equals("true")) {
                    tv_country.setTextColor(getResources().getColor(R.color.colorDarkText));
                } else {
                    tv_country.setTextColor(getResources().getColor(R.color.colorLightText));

                }


                switch (Server.GetImage()) {
                    case "japan":
                        iv_flag.setImageResource(R.drawable.ic_flag_japan);
                        break;
                    case "russia":
                        iv_flag.setImageResource(R.drawable.ic_flag_russia);
                        break;
                    case "southkorea":
                        iv_flag.setImageResource(R.drawable.ic_flag_south_korea);
                        break;
                    case "thailand":
                        iv_flag.setImageResource(R.drawable.ic_flag_thailand);
                        break;
                    case "vietnam":
                        iv_flag.setImageResource(R.drawable.ic_flag_vietnam);
                        break;
                    case "unitedstates":
                        iv_flag.setImageResource(R.drawable.ic_flag_united_states);
                        break;
                    case "unitedkingdom":
                        iv_flag.setImageResource(R.drawable.ic_flag_united_kingdom);
                        break;
                    case "singapore":
                        iv_flag.setImageResource(R.drawable.ic_flag_singapore);
                        break;
                    case "france":
                        iv_flag.setImageResource(R.drawable.ic_flag_france);
                        break;
                    case "germany":
                        iv_flag.setImageResource(R.drawable.ic_flag_germany);
                        break;
                    case "canada":
                        iv_flag.setImageResource(R.drawable.ic_flag_canada);
                        break;
                    case "luxemburg":
                        iv_flag.setImageResource(R.drawable.ic_flag_luxemburg);
                        break;
                    case "netherlands":
                        iv_flag.setImageResource(R.drawable.ic_flag_netherlands);
                        break;
                    case "spain":
                        iv_flag.setImageResource(R.drawable.ic_flag_spain);
                        break;
                    case "finland":
                        iv_flag.setImageResource(R.drawable.ic_flag_finland);
                        break;
                    case "poland":
                        iv_flag.setImageResource(R.drawable.ic_flag_poland);
                        break;
                    case "australia":
                        iv_flag.setImageResource(R.drawable.ic_flag_australia);
                        break;
                    case "italy":
                        iv_flag.setImageResource(R.drawable.ic_flag_italy);
                        break;
                    default:
                        iv_flag.setImageResource(R.drawable.ic_flag_unknown_mali);
                        break;
                }


                ll_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ll_item.setBackgroundColor(getResources().getColor(R.color.colorSelectItem));
                        tv_country.setTextColor(getResources().getColor(R.color.colorDarkText));
                        EncryptData En = new EncryptData();
                        try {
                            SharedPreferences SharedAppDetails = getSharedPreferences("connection_data", 0);
                            SharedPreferences.Editor Editor = SharedAppDetails.edit();
                            Editor.putString("id", Server.GetID());
                            Editor.putString("file_id", Server.GetFileID());
                            Editor.putString("file", En.encrypt(FileArray[Integer.valueOf(Server.GetFileID())][1]));
                            Editor.putString("city", Server.GetCity());
                            Editor.putString("country", Server.GetCountry());
                            Editor.putString("image", Server.GetImage());
                            Editor.putString("ip", Server.GetIP());
                            Editor.putString("active", Server.GetActive());
                            Editor.putString("signal", Server.GetSignal());
                            Editor.apply();
                            App.hasFile = true;
                            App.abortConnection = true;
                        } catch (Exception e) {
                            Bundle params = new Bundle();
                            params.putString("device_id", App.device_id);
                            params.putString("exception", "SA6" + e.toString());
                            mFirebaseAnalytics.logEvent("app_param_error", params);
                        }
                        finish();
                        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                    }
                });


                if (Server.GetSignal().equals(null)) {
                    iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_low);
                } else {
                    if (Server.GetSignal().equals("a")) {
                        iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_full);
                    } else if (Server.GetSignal().equals("b")) {
                        iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_normal);
                    } else if (Server.GetSignal().equals("c")) {
                        iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_medium);
                    } else if (Server.GetSignal().equals("d")) {
                        iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_low);
                    } else {
                        iv_signal_strength.setBackgroundResource(R.drawable.ic_signal_low);
                    }
                }
            }


            SharedPreferences ConnectionDetails = getSharedPreferences("connection_data", 0);
            int ID = Integer.valueOf(ConnectionDetails.getString("id", "1"));

            if (position == ID) {
                v.setBackgroundColor(getResources().getColor(R.color.colorSelectItem));
            } else {
                if (DarkMode.equals("true")) {
                    v.setBackgroundColor(getResources().getColor(R.color.colorDarkBackground));
                } else {
                    v.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));

                }
            }


            return v;
        }
    }


    private class Server {
        // {"id":0, "file":0, "city":"Essen","country":"Germany","image":"germany","ip":"51.68.191.75","active":"true","signal":"a"},
        private String ID;
        private String FileID;
        private String City;
        private String Country;
        private String Image;
        private String IP;
        private String Active;
        private String Signal;

        private String GetID() {
            return ID;
        }

        private void SetID(String ID) {
            this.ID = ID;
        }

        private String GetFileID() {
            return FileID;
        }

        private void SetFileID(String FileID) {
            this.FileID = FileID;
        }

        private String GetCity() {
            return City;
        }

        private void SetCity(String City) {
            this.City = City;
        }

        private String GetCountry() {
            return Country;
        }

        private void SetCountry(String Country) {
            this.Country = Country;
        }

        private String GetImage() {
            return Image;
        }

        private void SetImage(String Image) {
            this.Image = Image;
        }

        private String GetIP() {
            return IP;
        }

        private void SetIP(String IP) {
            this.IP = IP;
        }

        private String GetActive() {
            return Active;
        }

        private void SetActive(String Active) {
            this.Active = Active;
        }

        private String GetSignal() {
            return Signal;
        }

        private void SetSignal(String Signal) {
            this.Signal = Signal;
        }
    }


}
