package vn.lcsoft.luongchung.ftuschedule;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import vn.lcsoft.luongchung.models.LichPhanMang;
import vn.lcsoft.luongchung.models.MonHoc;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.DB_NAME;
import static vn.lcsoft.luongchung.StaticCode.ShowLog;
import static vn.lcsoft.luongchung.StaticCode.URL_WRU;
import static vn.lcsoft.luongchung.StaticCode.sf;

public class ImportDataOldSystem extends Activity {

    static int CODE_REQUEST = 1995;
    ArrayList<MonHoc> arrReadHTML;
    ArrayList<LichPhanMang> arrSeparate;
    String LinkURL;
    SharedPreferences sharedPreferences;
    String txtHtml = "", txtHtml1 = "", tmpURL;
    WebView webview;
    AlertDialog dialog;
    Dialog dialog_custom;
    SQLiteDatabase sqLiteDatabase;
    ContentValues row_sql;
    Boolean isLock = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_tu_dong);
        sharedPreferences = getSharedPreferences(DB_APP, Context.MODE_PRIVATE);
        try {
            addControls();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        addEvents();
        ViewPump.init(ViewPump.builder()
            .addInterceptor(new CalligraphyInterceptor(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/fontmain.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()))
            .build());

    }



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void addEvents() {

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                dialog.dismiss();
                tmpURL = url;
                webview.loadUrl("javascript:window.HtmlViewer.showHTML1" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                webview.loadUrl("javascript:android.onData(enrolledCourseSubjects)");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                tmpURL = url;
                webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });
        dialog.show();
        webview.loadUrl(LinkURL);
        LinearLayout _BtnSuaLink, _btnLoad, _btnTaiLich;

        _BtnSuaLink = findViewById(R.id.btn2);
        _BtnSuaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentChangeLink = new Intent(ImportDataOldSystem.this, ChangeURL.class);
                startActivityForResult(intentChangeLink, CODE_REQUEST);
            }
        });

        _btnLoad = findViewById(R.id.btn3);
        _btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPage();
            }
        });


        _btnTaiLich = findViewById(R.id.btn4);
        _btnTaiLich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpImport();
            }
        });
    }

    private void loadPage() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.luuURL), MODE_PRIVATE);
        String url = sharedPreferences.getString("URL", URL_WRU);
        dialog.show();
        webview.loadUrl(url);
    }

    private void popUpImport() {
        new AlertDialog.Builder(ImportDataOldSystem.this).setTitle("CHÚ Ý")
                .setMessage(getString(R.string.hoi))
                .setPositiveButton("có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Delete_Data_old();
                        boolean ck = true;
                        ck = xulyImport();
                        if (ck) {
                            Intent intent = new Intent(ImportDataOldSystem.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    }
                }).setNegativeButton("không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean ck = true;
                        ck = xulyImport();
                        if (ck) {
                            Intent intent = new Intent(ImportDataOldSystem.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }
        ).show();
    }

    private void Delete_Data_old() {
        try {
            sqLiteDatabase = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
            sqLiteDatabase.execSQL("DELETE FROM tbthoikhoabieu");
            sqLiteDatabase.close();
        } catch (Exception ignored) {
        }
    }


    private boolean xulyImport() {
        arrReadHTML = new ArrayList<>();
        arrSeparate = new ArrayList<>();
        sqLiteDatabase = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);

        ShowLog(this, "START IMPORT");
        //READ HTML ON PAGE
        if (txtHtml.equals("")) return false;
        Document doc = Jsoup.parse(txtHtml).clone();
        Elements elements = doc.select("td.tableborder tr.cssListItem");
        Elements elements1 = doc.select("td.tableborder tr.cssListAlternativeItem");


        for (int i = 0; i < elements.size(); i++) {
            Element e = elements.get(i);
            Elements row = e.select("td");
            try {
                String _TenLop = row.get(1).text();
                String _TenHocPhan = row.get(2).text();
                String _ThoiGian = row.get(3).text();
                String _Diadiem = row.get(4).text();
                String _SoTinChi = row.get(7).text();
                MonHoc monHoc = new MonHoc(_TenLop, _TenHocPhan, _ThoiGian, _Diadiem, _SoTinChi, "");
                arrReadHTML.add(monHoc);
            } catch (Exception ex) {
            }
        }


        for (int i = 0; i < elements1.size(); i++) {
            Element e = elements1.get(i);
            Elements row = e.select("td");
            try {
                String _TenLop = row.get(1).text();
                String _TenHocPhan = row.get(2).text();
                String _ThoiGian = row.get(3).text();
                String _Diadiem = row.get(4).text();
                String _SoTinChi = row.get(7).text();
                MonHoc monHoc = new MonHoc(_TenLop, _TenHocPhan, _ThoiGian, _Diadiem, _SoTinChi, "");
                arrReadHTML.add(monHoc);
            } catch (Exception ex) {
            }
        }

        ShowLog(this, "READ HTML is DONE: " + arrReadHTML.size());
        SimpleDateFormat sf_n = new SimpleDateFormat("dd/MM/yyyy");

        //SEPARATE
        for (int i = 0; i < arrReadHTML.size(); i++) {
            MonHoc monHoc = arrReadHTML.get(i);
            String TenMonHoc = monHoc.getTenLop();
            String TenLop = monHoc.getTenHocPhan();
            String DiaDiem = monHoc.getDiaDiems();
            String GiangVien = monHoc.getTenGiangVien();
            String SoTinChi = monHoc.getSoTinChi();
            for (int j = 0; j < arrReadHTML.get(i).getThoiGians().size(); j++) {
                String NgayBD = arrReadHTML.get(i).getThoiGians().get(j).getNgayBD();
                String NgayKT = arrReadHTML.get(i).getThoiGians().get(j).getNgayKT();
                try {
                    for (int k = 0; k < arrReadHTML.get(i).getThoiGians().get(j).getThuHocs().size(); k++) {
                        String ThuHoc = String.valueOf(arrReadHTML.get(i).getThoiGians().get(j).getThuHocs().get(k).getThu());
                        String TietBD = String.valueOf(arrReadHTML.get(i).getThoiGians().get(j).getThuHocs().get(k).getTietBD());
                        String TietKT = String.valueOf(arrReadHTML.get(i).getThoiGians().get(j).getThuHocs().get(k).getTietKT());
                        Date ngaybd, ngaykt;
                        try {
                            ngaybd = sf_n.parse(NgayBD);
                            ngaykt = sf_n.parse(NgayKT);
                        } catch (ParseException e) {
                            continue;
                        }
                        LichPhanMang a = new LichPhanMang(1, TenMonHoc, TenLop, DiaDiem, GiangVien, ngaybd, ngaykt, ThuHoc, TietBD, TietKT, SoTinChi);
                        arrSeparate.add(a);
                    }
                } catch (Exception ex) {
                    continue;
                }

            }
        }

        ShowLog(this, "READ SEPARATE is DONE: " + arrSeparate.size());
        //PUT INTO DB
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
                    row_sql.put("ThuHoc", arrSeparate.get(i).getThuHoc().toString());
                    row_sql.put("TietBatDau", arrSeparate.get(i).getTietBatDau().toString());
                    row_sql.put("TietKetThuc", arrSeparate.get(i).getTietKetThuc().toString());
                    sqLiteDatabase.insert("tbthoikhoabieu", null, row_sql);
                    ShowLog(this, "Insert: " + arrSeparate.get(i).toString());
                }
                c.add(Calendar.DATE, 1);
            }
        }
        ShowLog(this, "END IMPORT");
        return true;

    }


    private void addControls() throws ParseException {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.luuURL), MODE_PRIVATE);
        LinkURL = sharedPreferences.getString("URL", getString(R.string.linkdangky));
        dialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog_custom = new Dialog(ImportDataOldSystem.this);
        webview = findViewById(R.id.wv_TuDong);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        webview.addJavascriptInterface(this, "HtmlViewer");
        webview.addJavascriptInterface(this, "android");
    }

    @JavascriptInterface
    public void showHTML(String html) {
        txtHtml = html;
    }

    @JavascriptInterface
    public void onData(String value) {
        ShowLog(this, "Biến:" + value);
    }

    @JavascriptInterface
    public void showHTML1(String html) {
        isLock = true;
        if (html.contains("Tiết")) {
            ShowLog(this, "Unlock");
            isLock = false;
        }
        txtHtml1 = html;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQUEST) {
            loadPage();
        }
    }
}
