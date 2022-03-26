package vn.lcsoft.luongchung.ftuschedule;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class DetailsSchedule extends Activity {
    private TextView TenMon, TenLopTC, TietHoc, ThoiGian, SoTC, tengv;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_lich_hoc);
        addControls();
    }

    @SuppressLint("SetTextI18n")
    private void addControls() {
        TenMon = findViewById(R.id.id_tenmon);
        TenLopTC = findViewById(R.id.id_tenloptinchi);
        ThoiGian = findViewById(R.id.id_thoigianhoc);
        SoTC = findViewById(R.id.id_sotinchi);
        TietHoc = findViewById(R.id.id_tiethoc);
        tengv = findViewById(R.id.id_tengv);
        intent = getIntent();
        TenMon.setText(intent.getStringExtra("TenMonHoc"));
        TenLopTC.setText("Tên lớp tín chỉ: " + intent.getStringExtra("TenLopTC"));
        SoTC.setText("Địa điểm: " + intent.getStringExtra("DiaDiem"));
        TietHoc.setText("Tiết học: " + intent.getStringExtra("TietBD") + "-->" + intent.getStringExtra("TietKT"));
        ThoiGian.setText("Ngày học: " + intent.getStringExtra("NgayHoc"));

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/fontmain.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        tengv.setText("GV: "+intent.getStringExtra("TenGV"));

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
}
