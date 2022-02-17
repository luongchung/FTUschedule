package vn.lcsoft.luongchung;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import vn.lcsoft.luongchung.ftuschedule.BuildConfig;

public class StaticCode {
    //check run One time
    public static boolean ADSCONFIG = true;
    public static boolean ALLCONFIG = true;
    public static final int JOBID = 13111993;
    public static int verJson;
    public static final String DB_PATH = "/databases/";
    public static final String DB_NAME = "dbthoikhoabieu.sqlite";
    public static final String DB_APP = "TLUEDU";
    public static final String isFist = "isFist";
    public static final String isAdsForConfig = "isAds";
    public static final String isAdsForCode = "isAds1";
    public static final String isSync = "isSync";
    public static final String isPayADS = "isPayADS";
    public static final String versionJson = "versionJson";
    public static final String pathJsonConfig = "pathJsonConfig";
    public static boolean readedADS = false;
    public static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String URL_CF = "https://luongchung.github.io/doc/config.json";
    public static int timeSchedule = 12 * 60 * 60 * 1000;
    //list updateConfig
    public static String URL_WRU = "http://dangky.tlu.edu.vn";
    public static String URLQQB = "https://luongchung.github.io/qbee/a10.gif";
    public static String URL_AUTHEN = "http://ftugate.ftu.edu.vn/api/auth/login";
    public static String URL_GETYEAR = "http://ftugate.ftu.edu.vn/api/sch/w-locdshockytkbuser";
    public static String URL_GETSCHEDULE = "http://ftugate.ftu.edu.vn/api/sch/w-locdstkbhockytheodoituong";
    public static String URL_HUONGDAN = "https://www.youtube.com/watch?v=rcgns6Q6O-c";
    public static String URL_TLU = "http://sinhvien.tlu.edu.vn";
    public static String VC = "noads10form";
    public static String LOGINNOPASS = "NO";
    public static int NUMCOUNTADS = 20;


    public static String readfile(Context context, String namefile) {
        String tContents = "";
        try {
            InputStream stream = context.getAssets().open(namefile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {}
        return tContents;

    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static void ShowLog(Object obj, String log) {
        if(BuildConfig.DEBUG) Log.d("APP_TLU -- " + obj.getClass().getName() + ": ", log);
    }
}
