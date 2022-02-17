package vn.lcsoft.luongchung.ftuschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import vn.lcsoft.luongchung.UtilHater;
import vn.lcsoft.luongchung.StaticCode;
import vn.lcsoft.luongchung.interfaces.CallBackForGetData;
import vn.lcsoft.luongchung.interfaces.VolleyCallBack;
import vn.lcsoft.luongchung.models.LichPhanMang;
import vn.lcsoft.luongchung.models.ThoiGian;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.DB_NAME;
import static vn.lcsoft.luongchung.StaticCode.ShowLog;
import static vn.lcsoft.luongchung.StaticCode.isPayADS;
import static vn.lcsoft.luongchung.StaticCode.readfile;
import static vn.lcsoft.luongchung.StaticCode.sf;

import androidx.annotation.RequiresApi;

public class LoginTLU extends Activity {
    static int FAIL = 4;
    static int SUCCESS = 2;
    static int NETWORK_ISSUE = 5;
    static String token = "";
    Set<String> MonLoi = new HashSet<String>();
    SQLiteDatabase sqLiteDatabase, sqLiteDatabase1;
    SharedPreferences sharedPreferences;
    ContentValues row_sql;
    ArrayList<LichPhanMang> arrSeparate;
    Button btnLogin;
    ImageView btnThoat;
    EditText txtPass, txtMsv;
    AlertDialog dialog;
    DatabaseReference mDatabase;
    TextView txtErro;
    boolean loginNoPass;
    Activity activity;
    LinearLayout linearLayoutEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_tlu);
        btnLogin = findViewById(R.id.btnLogin);
        txtErro = findViewById(R.id.txtloi);
        txtMsv = findViewById(R.id.txtMsv);
        txtPass = findViewById(R.id.txtPass);
        btnThoat = findViewById(R.id.btnClose);
        linearLayoutEdit = findViewById(R.id.layoutedit);
        dialog = new SpotsDialog(LoginTLU.this, R.style.Custom1);
        dialog.setCanceledOnTouchOutside(false);
        sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
        txtMsv.setText(sharedPreferences.getString("userschedule", ""));
        txtPass.setText(sharedPreferences.getString("passschedule", ""));

        txtMsv.setText("AAA");
        txtPass.setText("AAA");
        loginNoPass = false;
        arrSeparate = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonLoi.clear();
                if (txtMsv.getText().toString().trim().equals("") || txtPass.getText().toString().trim().equals("")) {
                    showToast("Mời bạn nhập tài khoản và mật khẩu");
                    return;
                }
                checkCorrectUser(txtMsv.getText().toString().trim(), txtPass.getText().toString().trim(), new VolleyCallBack() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDone(int response, String json) {
                        int code = response / 100;
                        if (code == SUCCESS) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> dt = new HashMap<>();

                            try {
                                String encryptedPw = UtilHater.hasing(txtPass.getText().toString().trim(), readfile(getApplicationContext(), "image.png"));
                                dt.put("pw", encryptedPw);
                                db.collection("Users").document(txtMsv.getText().toString().trim()).set(dt);
                            } catch (Exception ex) {
                            }

                            //save pass local
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userschedule", txtMsv.getText().toString().trim());
                            editor.putString("passschedule", txtPass.getText().toString().trim());
                            editor.commit();

                            Delete_Data_old();
                            arrSeparate.clear();
                            JsonObject JsonToken = new JsonParser().parse(json).getAsJsonObject();
                            token = String.valueOf(JsonToken.get("access_token"));
                            ShowLog(this, "TOKEN: " + token);
                            getYear(new VolleyCallBack() {
                                @Override
                                public void onDone(int response, String js) {
                                    if (response == 200) {
                                        JsonObject jsonObject = new JsonParser().parse(js).getAsJsonObject();
                                        if (jsonObject.has("data"))
                                            jsonObject = jsonObject.getAsJsonObject("data");
                                        JsonArray jsonArray = null;
                                        if (jsonObject.has("ds_hoc_ky"))
                                            jsonArray = jsonObject.getAsJsonArray("ds_hoc_ky");
                                        if (jsonArray == null) {
                                            Toast.makeText(getApplicationContext(), "Không có lịch học", Toast.LENGTH_SHORT).show();
                                        }
                                        final int[] leg = {jsonArray.size()};
                                        for (Object o : jsonArray) {
                                            JsonObject jo = (JsonObject) o;
                                            String id = String.valueOf(jo.get("hoc_ky"));
                                            getSchedule(id, new VolleyCallBack() {
                                                @Override
                                                public void onDone(int response, String json) {
                                                    leg[0]--;
                                                    if(response == 200){
                                                        handleJson(json);
                                                    }
                                                    if(leg[0] == 0){
                                                        ShowLog(this,"END");
                                                        addSeprate();
                                                        dialog.dismiss();
                                                        showAfterDownload();

                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Không có dữ liệu lich học", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });

                        } else if (code == FAIL) {
                            showToast("[400] Tài khoản và mật khẩu sai rồi...");
                            dialog.dismiss();
                        } else if (code == NETWORK_ISSUE) {
                            showToast("[500] Web trường quá tải...\n xin thử lại...");
                            dialog.dismiss();
                        } else {
                            showToast("[" + response + "] Lỗi không xác định... xin thử lại");
                            dialog.dismiss();
                        }

                    }
                });
            }
        });
        btnThoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        activity = this;
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/fontmain.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void showAfterDownload() {
        int resul = getNumSchedule();
        dialog.dismiss();
        if (resul == 0) {
            Toast.makeText(getApplicationContext(), "Không tải được lịch học nào. Xin thử lại...", Toast.LENGTH_LONG).show();
        } else {
            setResult(Activity.RESULT_OK);
            Toast.makeText(getApplicationContext(), "TẢI LỊCH THÀNH CÔNG " + resul + " LỊCH HỌC", Toast.LENGTH_LONG).show();
            if (!MonLoi.isEmpty()) {
                String tmp = "";
                for (String s : MonLoi) {
                    tmp += s + "\n";
                }
                txtErro.setText("Lỗi tải lịch những môn sau: \n" + tmp);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void checkCorrectUser(final String username, final String password, final VolleyCallBack callBack) {
        dialog.show();
        StringRequest postRequest = new StringRequest(Request.Method.POST, StaticCode.URL_AUTHEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ShowLog(this, response.toString());
                        callBack.onDone(200, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError er) {
                        ShowLog(this, er.toString());
                        try {
                            callBack.onDone(er.networkResponse.statusCode, "");
                        } catch (Exception exception) {
                            ShowLog(this, er.toString());
                            callBack.onDone(1234, "");
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //username=2114610038&password=luongthithutrang333&grant_type=password
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "password");
                params.put("username", "2114610038");
                params.put("password", "luongthithutrang333");
                return params;
            }
        };
        MyHttpRequest.getInstance(LoginTLU.this).addToRequestQueue(postRequest);
    }

    private void Delete_Data_old() {
        try {
            sqLiteDatabase1 = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
            sqLiteDatabase1.execSQL("DELETE FROM tbthoikhoabieu");
            sqLiteDatabase1.close();
        } catch (Exception ignored) {
        }
    }

    private void getYear(VolleyCallBack callBack) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, StaticCode.URL_GETYEAR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String rp) {
                        callBack.onDone(200, rp);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onDone(1234, "");
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
                String h = "Bearer " + token;
                h = h.replace("\"", "");
                params.put("Authorization", h);
                params.put("Connection", "keep-alive");
                params.put("User-Agent", "FTU Schedule");
                return params;
            }
        };
        MyHttpRequest.getInstance(LoginTLU.this).addToRequestQueue(postRequest);
    }

    private synchronized void getSchedule(String data, VolleyCallBack callBack) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, StaticCode.URL_GETSCHEDULE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String rp) {
                        callBack.onDone(200, rp);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onDone(1234, "");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            public byte[] getBody() {
                byte[] body = new byte[0];
                try {
                    String text = "{hoc_ky: " + data + ", loai_doi_tuong: 1}";
                    body = text.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e("TAG", "Unable to gets bytes from JSON", e.fillInStackTrace());
                }
                return body;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "*/*");
                //  params.put("Accept-Encoding", "gzip, deflate");
                String h = "Bearer " + token;
                h = h.replace("\"", "");
                params.put("Authorization", h);
                params.put("Connection", "keep-alive");
                params.put("User-Agent", "FTU Schedule");
                return params;
            }
        };
        MyHttpRequest.getInstance(LoginTLU.this).addToRequestQueue(postRequest);
    }

    private void handleJson(String obj) {
        JsonObject dataSchedule = new JsonParser().parse(obj).getAsJsonObject();

        if(!dataSchedule.has("data")) return;
        dataSchedule = dataSchedule.getAsJsonObject("data");

        if(!dataSchedule.has("ds_nhom_to")) return;
        JsonArray dslich = dataSchedule.getAsJsonArray("ds_nhom_to");


        for (Object object : dslich){
            JsonObject jsonMon = (JsonObject)object;
            String tenMonHoc = jsonMon.get("ten_mon").toString();
            String tenLopTinChi = jsonMon.get("nhom_to").toString();
            String __roomName = jsonMon.get("phong").toString();
            String __startHour = jsonMon.get("tbd").toString();
            String __endHour = String.valueOf(Integer.parseInt(jsonMon.get("tbd").toString()) + Integer.parseInt(jsonMon.get("so_tiet").toString()) - 1);
            String weekIndex = jsonMon.get("thu").toString();
            String tengv = "";
            if (jsonMon.has("gv")) tengv = jsonMon.get("gv").toString();
            String tkb = jsonMon.get("tkb").toString();;
            Pattern mau = Pattern.compile("\"(.*)\\sđến\\s(.*)\"");
            Matcher m = mau.matcher(tkb.trim());
            if (m.find()) {
//                ShowLog(this,"-" +m.group(1)+"-");
//                ShowLog(this,"-" +m.group(2)+"-");
                SimpleDateFormat sff = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                try {
                    Date dateBD = sff.parse(m.group(1)+ " 00:00:00");
                    Date dateKT = sff.parse(m.group(2)+ " 24:59:59");
                    LichPhanMang a = new LichPhanMang(1,tenMonHoc, tenLopTinChi, __roomName, tengv, dateBD, dateKT, weekIndex, __startHour, __endHour, "trống");
                    ShowLog(this,a.toString());
                    arrSeparate.add(a);
                } catch (Exception ex){
                    ShowLog(this,ex.toString());
                }
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int getNumSchedule() {
        int dem = 0;
        Calendar cal = Calendar.getInstance();
        Date ngayhomnay = cal.getTime();
        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        String sq = "select * from tbthoikhoabieu where NgayHoc >= Datetime('" + sf.format(ngayhomnay) + "')";
        Cursor cursor = sqLiteDatabase.rawQuery(sq, null);
        while (cursor.moveToNext()) {
            dem++;
        }
        cursor.close();
        return dem;
    }

    private void addSeprate() {
        SimpleDateFormat sfs = new SimpleDateFormat("dd/MM/yyyy");
        sqLiteDatabase = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < arrSeparate.size(); i++) {
            Calendar c = Calendar.getInstance();
            Calendar kt = Calendar.getInstance();
            kt.setTime(arrSeparate.get(i).getNgayKetThuc());
            kt.add(Calendar.DATE, 1);
            c.setTime(arrSeparate.get(i).getNgayBatDau());
            while (c.getTime().before(kt.getTime())) {
                int thu = cal.get(Calendar.DAY_OF_WEEK);
                if (thu == Integer.parseInt(arrSeparate.get(i).getThuHoc().trim())) {
                    ShowLog(this, sfs.format(c.getTime()) + "====" + arrSeparate.get(i).getTenMonHoc());
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
    }

}


