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
    private TextView TenMon, TenLopTC, TietHoc, ThoiGian, SoTC;
    Button btnZoom,btnZoom1,btnZoom2,btnZoom3;
    EditText txtZoom,txtZoom1,txtZoom2,txtZoom3;
    Intent intent;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_lich_hoc);
        addControls();
    }

    @SuppressLint("SetTextI18n")
    private void addControls() {
        sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
        TenMon = findViewById(R.id.id_tenmon);
        TenLopTC = findViewById(R.id.id_tenloptinchi);
        ThoiGian = findViewById(R.id.id_thoigianhoc);
        SoTC = findViewById(R.id.id_sotinchi);
        TietHoc = findViewById(R.id.id_tiethoc);
        intent = getIntent();
        TenMon.setText(intent.getStringExtra("TenMonHoc"));
        TenLopTC.setText("Tên lớp tín chỉ: " + intent.getStringExtra("TenLopTC"));
        SoTC.setText("Địa điểm: " + intent.getStringExtra("DiaDiem"));
        TietHoc.setText("Tiết học: " + intent.getStringExtra("TietBD") + "-->" + intent.getStringExtra("TietKT"));
        ThoiGian.setText("Ngày học: " + intent.getStringExtra("NgayHoc"));

        btnZoom = findViewById(R.id.btnLoginZoom);
        txtZoom = findViewById(R.id.txtzoom);

        btnZoom1 = findViewById(R.id.btnLoginZoom1);
        txtZoom1 = findViewById(R.id.txtzoom1);

        btnZoom2 = findViewById(R.id.btnLoginZoom2);
        txtZoom2 = findViewById(R.id.txtzoom2);

        btnZoom3 = findViewById(R.id.btnLoginZoom3);
        txtZoom3 = findViewById(R.id.txtzoom3);

        String temp = sharedPreferences.getString(intent.getStringExtra("TenMonHoc"), "");
        txtZoom.setText(temp);
        temp = sharedPreferences.getString(intent.getStringExtra("TenMonHoc") + 1, "");
        txtZoom1.setText(temp);
        temp = sharedPreferences.getString(intent.getStringExtra("TenMonHoc") + 2, "");
        txtZoom2.setText(temp);
        temp = sharedPreferences.getString(intent.getStringExtra("TenMonHoc") + 3, "");
        txtZoom3.setText(temp);


        btnZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtZoom.getText().toString().equals("")) {
                    Toast.makeText(DetailsSchedule.this, "Bạn chưa nhập ZOOM ID", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(intent.getStringExtra("TenMonHoc"), txtZoom.getText().toString());
                editor.commit();
                launchZoomUrl(txtZoom.getText().toString());
            }
        });


        btnZoom1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtZoom1.getText().toString().equals("")) {
                    Toast.makeText(DetailsSchedule.this, "Bạn chưa nhập ZOOM ID", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(intent.getStringExtra("TenMonHoc") + 1, txtZoom1.getText().toString());
                editor.commit();
                launchZoomUrl(txtZoom1.getText().toString());
            }
        });

        btnZoom2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtZoom2.getText().toString().equals("")) {
                    Toast.makeText(DetailsSchedule.this, "Bạn chưa nhập ZOOM ID", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(intent.getStringExtra("TenMonHoc") + 2, txtZoom2.getText().toString());
                editor.commit();
                launchZoomUrl(txtZoom2.getText().toString());
            }
        });

        btnZoom3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtZoom3.getText().toString().equals("")) {
                    Toast.makeText(DetailsSchedule.this, "Bạn chưa nhập ZOOM ID", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(intent.getStringExtra("TenMonHoc") + 3, txtZoom3.getText().toString());
                editor.commit();
                launchZoomUrl(txtZoom3.getText().toString());
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


    private void launchZoomUrl(String ID) {
        ID = ID.replaceAll("\\s+", "");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("zoomus://zoom.us/join?confno=" + ID)); //&pwd=ghR5nb
        if (intent.resolveActivity(getApplication().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(DetailsSchedule.this, "Bạn chưa cài Zoom metting rồi", Toast.LENGTH_LONG).show();
        }
    }
}
