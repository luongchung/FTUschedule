package vn.lcsoft.luongchung.models;


import java.io.Serializable;

/**
 * Created by LUONG CHUNG on 5/1/2017.
 */

public class ThuHoc implements Serializable {
    private int idThu;
    private int thu;
    private int tietBD;
    private int tietKT;

    ThuHoc(int idThu, int thu, int tietBD, int tietKT) {
        this.idThu = idThu;
        this.thu = thu;
        this.tietBD = tietBD;
        this.tietKT = tietKT;
    }

    public int getThu() {
        return thu;
    }

    public int getTietBD() {
        return tietBD;
    }

    public int getTietKT() {
        return tietKT;
    }
}
