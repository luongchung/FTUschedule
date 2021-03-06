package vn.lcsoft.luongchung.ftuschedule;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import vn.lcsoft.luongchung.StaticCode;
import vn.lcsoft.luongchung.adapters.ViewPagerAdapter;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.ShowLog;
import static vn.lcsoft.luongchung.StaticCode.isAdsForConfig;
import static vn.lcsoft.luongchung.StaticCode.isAdsForCode;
import static vn.lcsoft.luongchung.StaticCode.isFist;
import static vn.lcsoft.luongchung.StaticCode.isPayADS;
import static vn.lcsoft.luongchung.StaticCode.isSync;
import static vn.lcsoft.luongchung.StaticCode.readfile;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivityTAG";
    private TextView tv;
    private ViewPager mViewPager;
    private NavigationTabStrip mCenterNavigationTabStrip;
    private InterstitialAd mInterstitialAd;
    SharedPreferences sharedPreferences;
    DatabaseReference mDatabase;
    private boolean ennableADSFromConfig = false;
    private boolean enableAdsFromCode = true;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        setUI();
        addthoigiantiethoc();
        xuLySaoChepSQLite();
        if(StaticCode.ADSCONFIG){
            StaticCode.ADSCONFIG = false;
            StaticCode.URL_TLU = readfile(getApplicationContext(), "fonts/vietfont.ttf");
        }

        ViewPump.init(ViewPump.builder()
            .addInterceptor(new CalligraphyInterceptor(
                    new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/fontmain.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()))
            .build());

        if(!sharedPreferences.getBoolean(isPayADS, false)){
            AdRequest adRequest = new AdRequest.Builder().build();
            ShowLog(this,"Run ADS");
            InterstitialAd.load(this, "ca-app-pub-5001443737686857/7029495090", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            ShowLog(this,"ADS loaded");
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    _unmuteSound();
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    ShowLog(this,"ADS l???i");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    mInterstitialAd = null;
                                    _muteSound();
                                    StaticCode.readedADS = true;

                                }
                            });
                            quangcao();
                        }


                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG, loadAdError.getMessage());
                            mInterstitialAd = null;
                        }
                    });
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initUI() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mViewPager = findViewById(R.id.vp);
        mCenterNavigationTabStrip = findViewById(R.id.nts_center);
        sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
        ennableADSFromConfig = sharedPreferences.getBoolean(isAdsForConfig, false); //get from database
        enableAdsFromCode = sharedPreferences.getBoolean(isAdsForCode, true); //user nh???p code
        mDatabase = FirebaseDatabase.getInstance().getReference();
        tv = findViewById(R.id.txtNameMain);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/fontmain.ttf");
        tv.setTypeface(face);
    }

    private void quangcao() {
        ShowLog(this, "Show ads: " + ennableADSFromConfig);
        if (ennableADSFromConfig && enableAdsFromCode) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(HomeActivity.this);
            } else {
                ShowLog(this, "ADS Ch??a load ???????c");
            }
        }
    }


    private void setUI() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new FragmentToday(), "H??M NAY");
        viewPagerAdapter.addFragments(new FragmentAll(), "T???T C???");
        viewPagerAdapter.addFragments(new FragmentSetting(), "C??I ?????T");
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(viewPagerAdapter);
        mCenterNavigationTabStrip.setViewPager(mViewPager, 0);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/fontmain.ttf");
        mCenterNavigationTabStrip.setTypeface(face);
    }

    private void addthoigiantiethoc() {

        Boolean isF = sharedPreferences.getBoolean(isFist, true);
        if (isF) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String[] arrTG = getResources().getStringArray(R.array.ThoiGianTiet);
            for (int i = 1; i <= arrTG.length; i++) {
                editor.putString(String.valueOf(i), arrTG[i - 1]);
            }
            editor.putBoolean(isFist, false);
            editor.apply();
        }
        FirebaseMessaging.getInstance().subscribeToTopic("TLUSCHEDULE");
    }

    private void xuLySaoChepSQLite() {
        File dbfile = getDatabasePath(StaticCode.DB_NAME);
        if (!dbfile.exists()) {
            try {
                saoChepDatabaseTuAsset();
                ShowLog(this, "Sao ch??p database th??nh c??ng !");
            } catch (Exception ex) {
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saoChepDatabaseTuAsset() {
        try {
            InputStream myInput = getAssets().open(StaticCode.DB_NAME);
            String outFileName = getApplicationInfo().dataDir + StaticCode.DB_PATH + StaticCode.DB_NAME;
            File f = new File(getApplicationInfo().dataDir + StaticCode.DB_PATH);
            if (!f.exists()) f.mkdir();//ch??a c?? ???????ng d???n th?? t???o ???????ng d???n database
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) myOutput.write(buffer, 0, length);
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception ex) {
            Toast.makeText(HomeActivity.this, "L???i sao ch??p d??? li???u", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void _unmuteSound() {
        AudioManager aManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
        aManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }

    private void _muteSound() {
        AudioManager aManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
        aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

}
