package vn.lcsoft.luongchung.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import vn.lcsoft.luongchung.models.GroupEx;
import vn.lcsoft.luongchung.models.LichChuan;
import vn.lcsoft.luongchung.ftuschedule.DetailsSchedule;
import vn.lcsoft.luongchung.ftuschedule.R;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;

/**
 * Created by LUONG CHUNG on 7/9/2017.
 */

public class AdapterExpand extends BaseExpandableListAdapter {

    private Activity context;
    private ArrayList<GroupEx> Me;
    private HashMap<GroupEx, ArrayList<LichChuan>> Con;
    ExpandableListView mExpandableListView;

    public AdapterExpand(Activity context, ArrayList<GroupEx> me, HashMap<GroupEx, ArrayList<LichChuan>> con) {
        this.context = context;
        Me = me;
        Con = con;
    }

    @Override
    public int getGroupCount() {
        if (Me != null) return Me.size();
        return 0;
    }

    @Override
    public int getChildrenCount(int i) {
        if (Me != null && Con != null) return Con.get(Me.get(i)).size();
        return 0;
    }

    @Override
    public Object getGroup(int i) {
        return Me.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return Con.get(Me.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        GroupEx s = Me.get(i);
        return s.getId();
    }

    @Override
    public long getChildId(int i, int i1) {
        LichChuan lichChuan = Con.get(Me.get(i)).get(i1);
        return lichChuan.getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(final int i, boolean b, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group, viewGroup, false);
        mExpandableListView = (ExpandableListView) viewGroup;
        mExpandableListView.expandGroup(i);
        TextView tv_name = view.findViewById(R.id.txt_Me);
        String nameG = Me.get(i).getName();
        tv_name.setText(nameG);

        return view;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_today, viewGroup, false);

        LichChuan lc =  Con.get(Me.get(i)).get(i1);
        if(lc.isC()){
            return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_leave, viewGroup, false);
        }
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(DB_APP, Context.MODE_PRIVATE);
        TextView txt_TenMonHoc = view.findViewById(R.id.txt_tenmonhoc);
        TextView txt_Diadiem = view.findViewById(R.id.txt_diadiem);
        TextView txt_thoigian = view.findViewById(R.id.txt_thoigian);
        txt_TenMonHoc.setText(lc.getTenMonHoc());
        txt_Diadiem.setText(lc.getDiaDiem());
        String kt = sharedPreferences.getString(lc.getTietBatDau(), "");
        txt_thoigian.setText(kt);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xulyclickitem(lc);

            }
        });
        return view;
    }

    private void xulyclickitem(LichChuan lichHomNay) {
        Intent intent = new Intent(this.context, DetailsSchedule.class);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
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
        context.startActivityForResult(intent, 1997);
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
