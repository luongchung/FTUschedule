package vn.lcsoft.luongchung.ftuschedule;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.ShowLog;
import static vn.lcsoft.luongchung.StaticCode.isAdsForCode;
import static vn.lcsoft.luongchung.StaticCode.NUMCOUNTADS;
import static vn.lcsoft.luongchung.StaticCode.isPayADS;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import vn.lcsoft.luongchung.StaticCode;

public class AdsActivity extends AppCompatActivity {

    Button btnGuiMa;
    Button btnThanhToanGG;
    TextView txtShowMaGioiThieu,txtttt;
    TextView txtSoGT;
    EditText txtMaGioiThieu;
    String tmp;
    DatabaseReference mDatabase;
    SharedPreferences sharedPreferences;
    private Activity activity;
    private BillingClient billingClient = null;
    private PurchasesUpdatedListener purchasesUpdatedListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        activity = this;
        btnGuiMa = findViewById(R.id.btnGuiMa);
        btnThanhToanGG = findViewById(R.id.btnThanhToanGG);
        txtMaGioiThieu = findViewById(R.id.txtMaGioThieu);
        txtShowMaGioiThieu = findViewById(R.id.txtShowMaGioiThieu);
        txtSoGT = findViewById(R.id.txtGT);
        txtttt = findViewById(R.id.txttttt);
        txtttt.setText("Gi???i thi???u v?? nh???p m?? c???a "+ NUMCOUNTADS +" ng?????i kh??c\nB???n s??? ???????c g??? b??? QU???NG C??O tr??n ???ng d???ng n??y");
        sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        tmp = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        txtShowMaGioiThieu.setText(tmp);
        setEvents();
        updatesTT();
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/fontmain.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());



        if(billingClient == null){
            if(purchasesUpdatedListener == null){
                purchasesUpdatedListener = new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                && purchases != null) {
                            for (Purchase purchase : purchases) {
                                ShowLog(this, purchase.toString());
                                Toast.makeText(activity, "THANH TO??N TH??NH C??NG.", Toast.LENGTH_LONG).show();
                                btnThanhToanGG.setText("QC ???? ???????c ???n tr??n ???ng d???ng n??y.");
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(isPayADS, true);
                                editor.apply();
                            }
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                            Toast.makeText(activity, "THANH TO??N TH???T B???I.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "THANH TO??N TH???T B???I.", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
            }
            billingClient = BillingClient.newBuilder(this)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build();
            connectChplay();
        }
    }

    void connectChplay(){
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    //ki???m tra c?? thanh to??n ch??a
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
                        @Override
                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                if(list.size() == 0){
                                    ShowLog(this,"ch??a mua : " + list.toString());
                                    getChitietbill();
                                } else {
                                    ShowLog(this,"???? mua : " + list.toString());
                                    btnThanhToanGG.setText("QC ???? ???????c ???n tr??n ???ng d???ng n??y.");
                                }
                            } else {
                                ShowLog(this,"Ch??a r?? : ");
                                btnThanhToanGG.setVisibility(View.INVISIBLE);
                            }

                        }
                    });

                }
                else {
                    btnThanhToanGG.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                connectChplay();
            }
        });
    }


    private void getChitietbill() {
        List<String> skuList = new ArrayList<>();
        skuList.add(StaticCode.VC);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK ){
                            SkuDetails info = skuDetailsList.get(0);
                            updateUI(info);
                        }
                    }
                });
    }
    void updateUI(SkuDetails info){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                btnThanhToanGG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(info)
                                .build();
                        billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
                    }
                });
                btnThanhToanGG.setText(info.getDescription() + " - "+ info.getPrice() + " /Th??ng");
            }
        });
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    private void updatesTT(){
        SharedPreferences sharedPreferences1 = getSharedPreferences(DB_APP, MODE_PRIVATE);
        int nt = sharedPreferences1.getInt("countGT", 0);
        txtSoGT.setText("(S??? l?????t ???? gi???i thi???u l??: "+nt+"/"+ NUMCOUNTADS +")");
    }

    private void setEvents() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.child("ADS").hasChild(tmp.toLowerCase())) {
                    mDatabase.child("ADS").child(tmp.toLowerCase()).setValue("false");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnGuiMa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String tmp2 = txtMaGioiThieu.getText().toString().toLowerCase();
                if(tmp2.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Vui l??ng ??i???n m?? gi???i thi???u...", Toast.LENGTH_LONG).show();
                    return;
                }

                if (tmp2.equals(tmp.toLowerCase())) {
                    Toast.makeText(getApplicationContext(), "Kh??ng ???????c nh???p m?? c???a b???n th??n.", Toast.LENGTH_LONG).show();
                } else {

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.child("ADS").hasChild(tmp2)) {
                                if (snapshot.child("ADS").child(tmp2).getValue().toString().equals("false")) {
                                    mDatabase.child("ADS").child(tmp2).setValue("true");
                                    SharedPreferences sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
                                    int NumberGt = sharedPreferences.getInt("countGT", 0);

                                    NumberGt = NumberGt + 1;
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("countGT", NumberGt);
                                    editor.apply();
                                    updatesTT();
                                    if (NumberGt >= NUMCOUNTADS || tmp2.contains("tlu")) {
                                        disableADS();
                                        Toast.makeText(AdsActivity.this, "Xin ch??c m???ng!!! b???n ???? ???????c t???t qu???ng c??o... \nKH???I ?????NG L???I ???NG D???NG", Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(AdsActivity.this, "Th??nh c??ng! B???n ???? gi???i thi???u th??m m???t ng?????i.", Toast.LENGTH_LONG).show();
                                    }
                                    //t??nh m?? GT
                                } else {
                                    Toast.makeText(AdsActivity.this, "M?? gi???i thi???u ???? s??? d???ng", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(AdsActivity.this, "M?? gi???i thi???u kh??ng ????ng", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }
        });

    }

    private void disableADS() {
        SharedPreferences sharedPreferences = getSharedPreferences(DB_APP, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(isAdsForCode, false);
        editor.apply();
    }

}