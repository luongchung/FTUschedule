package vn.lcsoft.luongchung.ftuschedule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import vn.lcsoft.luongchung.StaticCode;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSetting extends Fragment {

    LinearLayout btnThemLich1, btnLienHe,btnHuongDan,btnTTApp,btnADS;
    public FragmentSetting() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        btnThemLich1 = view.findViewById(R.id.btnThemLich1);
        btnLienHe = view.findViewById(R.id.btnLienHe);
        btnHuongDan = view.findViewById(R.id.btnHuongDan);
        btnTTApp = view.findViewById(R.id.btnTTApp);
        btnADS = view.findViewById(R.id.btnQuangCao);
        setEvents();
        return view;
    }



    private void setEvents() {
        btnThemLich1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginTLU.class);
                startActivity(intent);
            }
        });
        btnHuongDan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(StaticCode.URL_HUONGDAN));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.google.android.youtube");
                    startActivity(intent);
                } catch (Exception ex){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(StaticCode.URL_HUONGDAN));
                    startActivity(browserIntent);
                }
            }
        });
        btnTTApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), inforTG.class);
                startActivity(intent);
            }
        });
        btnADS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AdsActivity.class);
                startActivity(intent);
            }
        });
        btnLienHe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setPackage("com.facebook.orca");
                    intent.setData(Uri.parse("https://m.me/TLU.Schedule"));
                    startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(getContext(), "Bạn chưa cài Messenger..", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
