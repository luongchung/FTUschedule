package vn.lcsoft.luongchung.ftuschedule;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;
import vn.lcsoft.luongchung.StaticCode;
import vn.lcsoft.luongchung.adapters.AdapterLichHoc;
import vn.lcsoft.luongchung.models.LichChuan;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.DB_NAME;
import static vn.lcsoft.luongchung.StaticCode.ShowLog;
import static vn.lcsoft.luongchung.StaticCode.URL_CF;
import static vn.lcsoft.luongchung.StaticCode.isAdsForConfig;
import static vn.lcsoft.luongchung.StaticCode.isAdsForCode;
import static vn.lcsoft.luongchung.StaticCode.isPayADS;
import static vn.lcsoft.luongchung.StaticCode.isSync;
import static vn.lcsoft.luongchung.StaticCode.jsonToMap;
import static vn.lcsoft.luongchung.StaticCode.pathJsonConfig;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentToday extends Fragment implements Comparator<LichChuan> {
    private GifImageView gifImageView;
    private ArrayList<LichChuan> arrLich_HomNay;
    private ListView listView;
    private RelativeLayout relativeLayout;
    private View view;
    private AdapterLichHoc adapterLichHoc = null;
    private TextView titleToday;
    private boolean isHaveLinkToday = false;
    private String urlToday;
    SharedPreferences sharedPreferences;
    AdView mAdView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);
        listView = view.findViewById(R.id.lv_lichhomnay);
        relativeLayout = view.findViewById(R.id.bk_image);
        gifImageView = view.findViewById(R.id.giftoday);
        titleToday = view.findViewById(R.id.txtShow1);
        arrLich_HomNay = new ArrayList<>();
        adapterLichHoc = new AdapterLichHoc(getActivity(), R.layout.item_today, arrLich_HomNay);
        listView.setAdapter(adapterLichHoc);
        sharedPreferences = getActivity().getSharedPreferences(DB_APP, MODE_PRIVATE);
        if (StaticCode.ALLCONFIG) {
            StaticCode.ALLCONFIG = false;
            loadCurrentlyConFig();
            updateConfig();

        }
        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                enableAds(view);
            }
        });
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHaveLinkToday) {
                    try {
                        Intent defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
                        defaultBrowser.setData(Uri.parse(urlToday));
                        startActivity(defaultBrowser);
                    } catch (Exception ex) {
                    }

                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        new GetDataAll().execute();
    }

    public void updateConfig() {
        if (BuildConfig.DEBUG) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(StaticCode.readfile(getActivity(), "test.json"));
                if (jsonObject != null) {
                    writeJson(jsonToMap(jsonObject));
                    loadCurrentlyConFig();
                }
            } catch (JSONException err) {
                ShowLog(this, err.toString());
            }
            return;
        }
        StringRequest postRequest = new StringRequest(Request.Method.GET, URL_CF,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            if (jsonObject != null) {
                                writeJson(jsonToMap(jsonObject));
                                loadCurrentlyConFig();
                            }
                        } catch (JSONException err) {
                            ShowLog(this, err.toString());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ShowLog(this, error.toString());
                    }
                }
        );
        MyHttpRequest.getInstance(getActivity()).addToRequestQueue(postRequest);
    }

    private void writeJson(Map<String, Object> data) {
        if (data.containsKey("verj") && !data.get("verj").toString().equals("")) {
            int versionLatest = Integer.parseInt(data.get("verj").toString().trim());
            int currentVersion = sharedPreferences.getInt(StaticCode.versionJson, 0);
            StaticCode.verJson = currentVersion;
            ShowLog(this, "versionLatest: " + versionLatest + "   currentVersion: " + currentVersion);
            ShowLog(this, "DOWNlOAD: " + data.toString());
            if (versionLatest > currentVersion) {
                try {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(StaticCode.versionJson, versionLatest);
                    editor.putString(pathJsonConfig, (new JSONObject(data)).toString());
                    editor.commit();
                    ShowLog(this, "Cấu hình đã cập nhập bản: " + versionLatest);
                } catch (Exception ex) {
                    ShowLog(this, "Lỗi cập nhập cấu hình bản: " + versionLatest);
                }
            }
        }
    }

    @Override
    public int compare(LichChuan LichChuan, LichChuan t1) {
        if (Integer.parseInt(LichChuan.getTietBatDau()) > Integer.parseInt(t1.getTietBatDau()))
            return 1;
        else if (Integer.parseInt(LichChuan.getTietBatDau()) == Integer.parseInt(t1.getTietBatDau()))
            return 0;
        else
            return -1;
    }

    @SuppressLint("StaticFieldLeak")
    public class GetDataAll extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
            arrLich_HomNay.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            Date ngayhomnay = cal.getTime();
            SimpleDateFormat sfOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
            SQLiteDatabase sqLiteDatabase = getActivity().openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
            String sql = "select * from tbthoikhoabieu where NgayHoc BETWEEN Datetime('" + sfOnlyDay.format(ngayhomnay) + " 00:00:00') and Datetime('" + sfOnlyDay.format(ngayhomnay) + " 23:59:59')";
            Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                arrLich_HomNay.add(new LichChuan(Integer.parseInt(
                        cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4), date,
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getString(10)));
            }
            Collections.sort(arrLich_HomNay, new FragmentToday());
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (arrLich_HomNay.size() == 0) {
                        relativeLayout.setVisibility(View.VISIBLE);
                        return;
                    } else relativeLayout.setVisibility(View.INVISIBLE);
                    ArrayList<LichChuan> arrTD = new ArrayList<>(arrLich_HomNay);
                    adapterLichHoc = new AdapterLichHoc(getActivity(), R.layout.item_today, arrTD);
                    listView.setAdapter(adapterLichHoc);
                    adapterLichHoc.notifyDataSetChanged();
                }
            });
        }
    }

    public void enableAds(View view) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(DB_APP, MODE_PRIVATE);
        Boolean ennableADS = sharedPreferences.getBoolean(isAdsForConfig, false);
        Boolean ennableADS1 = sharedPreferences.getBoolean(isAdsForCode, true);

        if(!sharedPreferences.getBoolean(isPayADS, false)){
            if (ennableADS && ennableADS1) {
                mAdView = view.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(sharedPreferences.getBoolean(isPayADS, false)){
            if(mAdView != null){
                mAdView.destroy();
            }
        }
    }

    private void loadCurrentlyConFig() {
        try {
            String js = sharedPreferences.getString(pathJsonConfig, "{}");
            ShowLog(this, "LOADING: " + js);
            Map<String, Object> mapConfig = null;
            JSONObject jsonObject = new JSONObject(js);
            if (jsonObject == null) return;
            mapConfig = jsonToMap(jsonObject);
            if (mapConfig == null) return;
            String android_id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;


            if (mapConfig != null && !mapConfig.isEmpty()) {
                if (mapConfig.containsKey("ver") && !mapConfig.get("ver").toString().equals("") && mapConfig.get("ver").toString().contains(versionName)) {
                    if (mapConfig.containsKey("Image") && !mapConfig.get("Image").toString().equals(""))
                        StaticCode.URLQQB = mapConfig.get("Image").toString();
                    if (mapConfig.containsKey("sub") && !mapConfig.get("sub").toString().equals(""))
                        titleToday.setText(mapConfig.get("sub").toString());
                    if (mapConfig.containsKey("click") && !mapConfig.get("click").toString().equals("")) {
                        urlToday = mapConfig.get("click").toString();
                        isHaveLinkToday = true;
                    }
                }
                if (mapConfig.containsKey("LOGINNOPASS") && !mapConfig.get("LOGINNOPASS").toString().equals("") ) {
                    StaticCode.LOGINNOPASS = mapConfig.get("LOGINNOPASS").toString().trim();
                }
                if (mapConfig.containsKey("verj") && !mapConfig.get("verj").toString().equals("") ) {
                    StaticCode.verJson = Integer.parseInt(mapConfig.get("verj").toString().trim());
                }
                if (mapConfig.containsKey("URL_WRU") && !mapConfig.get("URL_WRU").toString().equals("")) {
                    StaticCode.URL_WRU = mapConfig.get("URL_WRU").toString().trim();
                }
                if (mapConfig.containsKey("URLQQB") && !mapConfig.get("URLQQB").toString().equals("")) {
                    StaticCode.URLQQB = mapConfig.get("URLQQB").toString().trim();
                }
                if (mapConfig.containsKey("NUMCOUNTADS") && !mapConfig.get("NUMCOUNTADS").toString().equals("")) {
                    StaticCode.NUMCOUNTADS = Integer.parseInt(mapConfig.get("NUMCOUNTADS").toString().trim());
                }
                if (mapConfig.containsKey("URL_AUTHEN") && !mapConfig.get("URL_AUTHEN").toString().equals("")) {
                    StaticCode.URL_AUTHEN = mapConfig.get("URL_AUTHEN").toString();
                }
                if (mapConfig.containsKey("URL_GETSCHEDULE") && !mapConfig.get("URL_GETSCHEDULE").toString().equals("")) {
                    StaticCode.URL_GETSCHEDULE = mapConfig.get("URL_GETSCHEDULE").toString();
                }
                if (mapConfig.containsKey("URL_HUONGDAN") && !mapConfig.get("URL_HUONGDAN").toString().equals("")) {
                    StaticCode.URL_HUONGDAN = mapConfig.get("URL_HUONGDAN").toString();
                }
                if (mapConfig.containsKey("VER_BLOCK") && !mapConfig.get("VER_BLOCK").toString().equals("") && mapConfig.get("VER_BLOCK").toString().contains(versionName)) {
                    new AlertDialog.Builder(getActivity()).setTitle("CHÚ Ý")
                            .setInverseBackgroundForced(true)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    getActivity().finish();
                                }
                            })
                            .setMessage("Hãy cập phiên bản mới để sử dụng tiện ích này.")
                            .setPositiveButton("Cập nhập tại đây", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }

                                }
                            }).setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            }
                    ).show();
                }
                if (mapConfig.containsKey("USER_BLOCK") && !mapConfig.get("USER_BLOCK").toString().equals("")
                        && (mapConfig.get("USER_BLOCK").toString().contains(android_id))
                        || mapConfig.get("USER_BLOCK").toString().contains(sharedPreferences.getString("userschedule", "XXXXXXXXXXX"))) {
                    new AlertDialog.Builder(getActivity()).setTitle("CHÚ Ý")
                            .setInverseBackgroundForced(true)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    getActivity().finish();
                                }
                            })
                            .setMessage("Tài khoản bạn đã bị khóa do vi phạm.")
                            .setPositiveButton("Tạm biệt", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();

                                }
                            }).setNegativeButton("Tạm biệt", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            }
                    ).show();
                }

                if (mapConfig.containsKey("sync") && !mapConfig.get("sync").toString().equals("")) {
                    if (mapConfig.get("sync").toString().equals("ON")) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(isSync, true);
                        editor.apply();
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(isSync, false);
                        editor.apply();
                    }
                    jsonObject.remove("sync");
                    SharedPreferences.Editor editor1 = sharedPreferences.edit();
                    editor1.putString(pathJsonConfig, jsonObject.toString());
                    editor1.commit();
                }
                if (mapConfig.containsKey("timesync") && !mapConfig.get("timesync").toString().equals("")) {
                    StaticCode.timeSchedule = Integer.parseInt(mapConfig.get("timesync").toString());
                }
                //enable qc by list tatca
//                if (mapConfig.containsKey("ads")) {
//                    Map<String, Object> mapAds = (Map<String, Object>) mapConfig.get("ads");
//                    if (mapAds.containsKey(String.valueOf(BuildConfig.VERSION_CODE))) {
//                        if (mapAds.get(String.valueOf(BuildConfig.VERSION_CODE)).toString().equals("OFF")) {
//                            ShowLog(this, "Deactive ADS from Server");
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.putBoolean(isAdsForConfig, false);
//                            editor.commit();
//                        } else {
//                            ShowLog(this, "Active ADS from Server");
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.putBoolean(isAdsForConfig, true);
//                            editor.commit();
//                        }
//                    }
//                }
            }
        } catch (Exception e) {
            ShowLog(this, "Lỗi đọc cấu hình" + e.toString());
        }

        final Animation fadeout = new AlphaAnimation(1.f, 1.f);
        fadeout.setDuration(4000); // You can modify the duration here
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Glide.with(getContext()).load(StaticCode.URLQQB).into(gifImageView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gifImageView.startAnimation(fadeout);
            }
        });
        gifImageView.startAnimation(fadeout);

    }

}
