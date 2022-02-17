package vn.lcsoft.luongchung.ftuschedule;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Notification extends Activity {
    WebView wv_Thongbao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);
        wv_Thongbao = findViewById(R.id.wv_thongbao);
        wv_Thongbao.getSettings().setJavaScriptEnabled(true);
        wv_Thongbao.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String data = bundle.getString("html");
            if (!data.equals("")) {
                wv_Thongbao.loadData(data, "text/html; charset=utf-8", "UTF-8");
            } else {
                String url = bundle.getString("url");
                wv_Thongbao.loadUrl(url);
            }
        }
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

}