package vn.lcsoft.luongchung.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LUONG CHUNG on 5/1/2017.
 */

public class ThoiGian implements Serializable {
    private String ngayBD;
    private String ngayKT;
    private ArrayList<ThuHoc> thuHocs;

    ThoiGian(String ngayBD, String ngayKT, String txtthuHoc) {
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
        this.thuHocs = getArray_ThuHoc(txtthuHoc);
    }

    public String getNgayBD() {
        return ngayBD;
    }

    public String getNgayKT() {
        return ngayKT;
    }

    public ArrayList<ThuHoc> getThuHocs() {
        return thuHocs;
    }

    private ArrayList<ThuHoc> getArray_ThuHoc(String s) {
        ArrayList<ThuHoc> temp = new ArrayList<>();
        Pattern cn = Pattern.compile("(.*)\\s(.*)\\stiết\\s(.*)");

        Pattern k = Pattern.compile("\\(");
        String[] tmp = k.split(s.trim());

        for (String u : tmp) {
            Matcher m = cn.matcher(u.trim());
            if (m.find()) {
                u = u.trim();
                if (u.length() > 0) {
                    int max_t = 0;
                    int min_t = 20;
                    String AllTiet = m.group(3);
                    String[] tiets = AllTiet.split(",");
                    for (String i : tiets) {
                        int index = Integer.parseInt(i);
                        if (index < min_t) min_t = index;
                        if (index > max_t) max_t = index;
                    }
                    if (u.contains("Chủ nhật")) {
                        temp.add(new ThuHoc(1, 1, min_t, max_t));
                    } else {
                        temp.add(new ThuHoc(1, Integer.parseInt(m.group(2).trim()), min_t, max_t));
                    }

                }
            }
        }
        return temp;
    }

}
