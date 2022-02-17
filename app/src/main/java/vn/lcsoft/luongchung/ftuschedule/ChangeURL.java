package vn.lcsoft.luongchung.ftuschedule;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ChangeURL extends Activity {
    Button btnChangeLink, btnBanDau;
    TextView txtLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_link);
        txtLink = findViewById(R.id.txturldangky);
        btnChangeLink = findViewById(R.id.btnLuuURL);
        btnBanDau = findViewById(R.id.btnDatLai);
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.luuURL), MODE_PRIVATE);
        btnChangeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("URL", txtLink.getText().toString());
                editor.commit();
                String url = sharedPreferences.getString("URL", getString(R.string.linkdangky));
                Toast.makeText(ChangeURL.this, "Đổi link thành công! " + url, Toast.LENGTH_LONG).show();
                finish();
            }
        });
        btnBanDau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("URL", getString(R.string.linkdangky));
                editor.commit();
                String url = sharedPreferences.getString("URL", getString(R.string.linkdangky));
                Toast.makeText(ChangeURL.this, "Đã đặt lại ban đầu! " + url, Toast.LENGTH_LONG).show();

            }
        });
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
