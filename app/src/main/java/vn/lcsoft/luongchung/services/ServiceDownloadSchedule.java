package vn.lcsoft.luongchung.services;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.DB_NAME;
import static vn.lcsoft.luongchung.StaticCode.ShowLog;
import static vn.lcsoft.luongchung.StaticCode.sf;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vn.lcsoft.luongchung.StaticCode;
import vn.lcsoft.luongchung.interfaces.CallBackForGetData;
import vn.lcsoft.luongchung.interfaces.VolleyCallBack;
import vn.lcsoft.luongchung.models.LichPhanMang;
import vn.lcsoft.luongchung.ftuschedule.MyHttpRequest;

@SuppressLint("NewApi")
public class ServiceDownloadSchedule extends JobService {
    private static String TAG = "ServiceDownloadSchedule";
    static int SUCCESS = 2;
    SQLiteDatabase sqLiteDatabase,sqLiteDatabase1;
    ContentValues row_sql;
    SharedPreferences sharedPreferences;
    ArrayList<LichPhanMang> arrSeparate = new ArrayList<>();;
    private int countNum = 16;
    private void getDataSubject(final JsonObject response, int index, CallBackForGetData callBack) {
        StringRequest postRequest = new StringRequest(Request.Method.GET, StaticCode.URL_GETSCHEDULE + index,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        handleJson(response);
                    } catch (Exception e) {}
                    callBack.onDone();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callBack.onDone();
                }
            }
        ) {
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            return params;
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> params = new HashMap<>();
            params.put("Accept", "application/json, text/plain, */*");
            params.put("Accept-Encoding", "gzip, deflate");
            String h = "Bearer " + response.get("access_token");
            h = h.replace("\"", "");
            params.put("Authorization", h);
            params.put("Connection", "keep-alive");
            params.put("User-Agent", "TLU Schedule");
            return params;
        }
        };
        MyHttpRequest.getInstance(ServiceDownloadSchedule.this).addToRequestQueue(postRequest);
    }
    private void checkCorrectUser(final String username, final String password, final VolleyCallBack callBack) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, StaticCode.URL_AUTHEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onDone(200, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError er) {
                        try{
                            callBack.onDone(er.networkResponse.statusCode, "");
                        } catch (Exception exception){
                            ShowLog(this,er.toString());
                            callBack.onDone(1234, "");
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", "education_client");
                params.put("grant_type", "password");
                params.put("username", username);
                params.put("password", password);
                params.put("client_secret", "password");
                params.put("User-Agent", "TLU Schedule");
                return params;
            }
        };
        MyHttpRequest.getInstance(ServiceDownloadSchedule.this).addToRequestQueue(postRequest);
    }
    private void Delete_Data_old() {
        try {
            sqLiteDatabase1 = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
            sqLiteDatabase1.execSQL("DELETE FROM tbthoikhoabieu");
            sqLiteDatabase1.close();
        } catch (Exception ignored) {
        }
    }
    private void handleJson(String obj) throws JSONException {
        JSONArray jsonArray = new JSONArray(obj);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            if (o.isNull("subjectCode")) continue;
            if (o.isNull("courseSubject")) continue;
            JSONObject courseSubject = (JSONObject) o.get("courseSubject");
            if (courseSubject.isNull("timetables")) continue;
            else {
                JSONArray timetables = (JSONArray) courseSubject.get("timetables");
                for (int j = 0; j < timetables.length(); j++) {
                    JSONObject tmp = timetables.getJSONObject(j);
                    if (tmp.isNull("endHour")) continue;
                    String endHour = tmp.get("endHour").toString();
                    JSONObject _endHour = new JSONObject(endHour);
                    if (_endHour.isNull("id")) continue;
                    String __endHour = _endHour.get("id").toString();
                    if (tmp.isNull("startHour")) continue;
                    String startHour = tmp.get("startHour").toString();
                    JSONObject _startHour = new JSONObject(startHour);
                    if (_startHour.isNull("id")) continue;
                    String __startHour = _startHour.get("id").toString();
                    if (tmp.isNull("roomName")) continue;
                    String __roomName = tmp.get("roomName").toString();
                    if (tmp.isNull("weekIndex")) continue;
                    String weekIndex = tmp.get("weekIndex").toString();
                    if (tmp.isNull("startDate")) continue;
                    if (tmp.isNull("endDate")) continue;
                    Date dateBD = new Date(Long.parseLong(tmp.get("startDate").toString()));
                    Date dateKT = new Date(Long.parseLong(tmp.get("endDate").toString()));
                    LichPhanMang a = new LichPhanMang(1, o.getString("subjectCode"), "Trống", __roomName, "Trống", dateBD, dateKT, weekIndex, __startHour, __endHour, "trống");
                    //ShowLog(this, "arrSeparate.add" + a.toString());
                    arrSeparate.add(a);
                }
            }
        }
    }

    public void getSyncSchedule(JobParameters jobParameters){
        ShowLog(this,"Begin update schedule from Services");
        sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
        String user = sharedPreferences.getString("userschedule", "");
        String pass = sharedPreferences.getString("passschedule", "");
        if(user.equals("") || pass.equals("")) {
            ShowLog(this,"No user for download");
            jobFinished(jobParameters,false);
        }
        checkCorrectUser(user, pass, new VolleyCallBack() {
            @Override
            public void onDone(int response, String json) {
                int code = response/100;
                if (code == SUCCESS) {
                    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                    arrSeparate.clear();
                    countNum = 16;
                    for (int i = 16; i > 0; i--) getDataSubject(jsonObject, i - 1, new CallBackForGetData() {
                        @Override
                        public void onDone() {
                            countNum--;
                            if(countNum == 0){
                                addSeparate();
                                jobFinished(jobParameters,true);
                                return;
                            }
                        }
                    });
                }
            }
        });
        jobFinished(jobParameters,true);

    }
    private void addSeparate() {
        if(arrSeparate.size() <= 0) return ;
        else Delete_Data_old();

        sqLiteDatabase = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < arrSeparate.size(); i++) {
            Calendar c = Calendar.getInstance();
            Calendar kt = Calendar.getInstance();
            kt.setTime(arrSeparate.get(i).getNgayKetThuc());
            kt.add(Calendar.DATE, 1);
            c.setTime(arrSeparate.get(i).getNgayBatDau());
            while (c.getTime().before(kt.getTime())) {
                cal.setTime(c.getTime());
                int thu = cal.get(Calendar.DAY_OF_WEEK);
                if (thu == Integer.parseInt(arrSeparate.get(i).getThuHoc().trim())) {
                    row_sql = new ContentValues();
                    row_sql.put("TenMonHoc", arrSeparate.get(i).getTenMonHoc() + "");
                    row_sql.put("TenLopTinChi", arrSeparate.get(i).getTenLopTinChi() + "");
                    row_sql.put("DiaDiem", arrSeparate.get(i).getDiaDiem() + "");
                    row_sql.put("GiangVien", arrSeparate.get(i).getGiangVien() + "");
                    row_sql.put("SoTinChi", arrSeparate.get(i).getSoTinChi() + "");
                    row_sql.put("NgayHoc", sf.format(c.getTime()));
                    row_sql.put("ThuHoc", arrSeparate.get(i).getThuHoc());
                    row_sql.put("TietBatDau", arrSeparate.get(i).getTietBatDau());
                    row_sql.put("TietKetThuc", arrSeparate.get(i).getTietKetThuc());
                    sqLiteDatabase.insert("tbthoikhoabieu", null, row_sql);
                }
                c.add(Calendar.DATE, 1);
            }
        }
        ShowLog(this,"End update schedule from Services");
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        getSyncSchedule(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}