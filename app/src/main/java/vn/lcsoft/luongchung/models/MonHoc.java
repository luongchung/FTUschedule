package vn.lcsoft.luongchung.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LUONG CHUNG on 5/1/2017.
 */

public class MonHoc implements Serializable {
    private String tenLop;
    private String tenHocPhan;
    private ArrayList<ThoiGian> thoiGians;
    private String diaDiems;
    private String soTinChi;
    private String tenGiangVien;

    public String getTenGiangVien() {
        return tenGiangVien;
    }


    public MonHoc(String tenLop, String tenHocPhan, String txtThoiGian, String txtDiaDiem, String soTinChi, String tenGiangVien) {
        this.tenLop = tenLop;
        this.tenHocPhan = tenHocPhan;
        this.thoiGians = getArray_ThoiGian(txtThoiGian);
        this.diaDiems = txtDiaDiem;
        this.soTinChi = soTinChi;
        this.tenGiangVien = tenGiangVien;
    }

    //<editor-fold desc="hàm get set">

    public String getTenLop() {
        return tenLop;
    }


    public String getTenHocPhan() {
        return tenHocPhan;
    }


    public ArrayList<ThoiGian> getThoiGians() {
        return thoiGians;
    }


    public String getDiaDiems() {
        return diaDiems;
    }


    public String getSoTinChi() {
        return soTinChi;
    }

    //</editor-fold>

    private ArrayList<ThoiGian> getArray_ThoiGian(String s) {
        ArrayList<ThoiGian> temp = new ArrayList<>();

        Pattern mau = Pattern.compile("(.*)\\sđến\\s(.*):(.*)");
        Pattern p = Pattern.compile("Từ");
        String[] str = p.split(s);
        for (String text : str) {
            Matcher m = mau.matcher(text.trim());
            if (m.find()) {
                temp.add(new ThoiGian(m.group(1), m.group(2), m.group(3)));
            }

        }

        return temp;
    }
}

