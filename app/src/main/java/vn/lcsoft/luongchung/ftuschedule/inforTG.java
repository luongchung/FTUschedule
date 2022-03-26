package vn.lcsoft.luongchung.ftuschedule;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.isSync;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import vn.lcsoft.luongchung.StaticCode;

public class inforTG extends AppCompatActivity {
    TextView txt, txtConfig;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infor_tg);
        txt = findViewById(R.id.id_version);
        txtConfig = findViewById(R.id.id_config);
        String versionName = "Phiên bản ứng dụng: ";
        String versionConfig = "Phiên bản cấu hình: " + StaticCode.verJson;
        try {
            versionName += getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
        }
        txt.setText(versionName);
        txtConfig.setText(versionConfig);

        sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
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