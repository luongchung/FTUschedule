package vn.lcsoft.luongchung.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import vn.lcsoft.luongchung.models.LichChuan;
import vn.lcsoft.luongchung.ftuschedule.DetailsSchedule;
import vn.lcsoft.luongchung.ftuschedule.R;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;

/**
 * Created by LUONG CHUNG on 7/7/2017.
 */

public class AdapterLichHoc extends ArrayAdapter<LichChuan> {
    private Activity context;
    private int resource;
    private List<LichChuan> objects;

    public AdapterLichHoc(Activity context, int resource, List<LichChuan> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = this.context.getLayoutInflater();
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(this.resource, null);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(DB_APP, Context.MODE_PRIVATE);

        TextView txt_Thoigian = view.findViewById(R.id.txt_thoigian);
        TextView txt_tenmonhoc = view.findViewById(R.id.txt_tenmonhoc);
        TextView txt_diadiem = view.findViewById(R.id.txt_diadiem);

        final LichChuan lichHomNay = objects.get(position);
        String kt = sharedPreferences.getString(lichHomNay.getTietBatDau(), "");
        txt_Thoigian.setText(kt);
        txt_tenmonhoc.setText(lichHomNay.getTenMonHoc());
        txt_diadiem.setText(lichHomNay.getDiaDiem());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xulyclickitem(lichHomNay);
            }
        });
        return view;
    }

    private void xulyclickitem(LichChuan lichHomNay) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Intent intent = new Intent(context, DetailsSchedule.class);
        intent.putExtra("ID", lichHomNay.getId());
        intent.putExtra("TenGV", lichHomNay.getGiangVien());
        intent.putExtra("TietKT", lichHomNay.getTietKetThuc());
        intent.putExtra("SoTC", lichHomNay.getSoTinChi());
        intent.putExtra("TenLopTC", lichHomNay.getTenLopTinChi());
        intent.putExtra("DiaDiem", lichHomNay.getDiaDiem());
        intent.putExtra("NgayHoc", simpleDateFormat.format(lichHomNay.getNgay()));
        intent.putExtra("TenMonHoc", lichHomNay.getTenMonHoc());
        intent.putExtra("ThuHoc", lichHomNay.getThuHoc());
        intent.putExtra("TietBD", lichHomNay.getTietBatDau());
        intent.putExtra("Note", lichHomNay.getNote());

        context.startActivityForResult(intent, 1996);

    }
}
