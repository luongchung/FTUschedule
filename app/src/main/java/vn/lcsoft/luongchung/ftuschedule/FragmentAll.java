package vn.lcsoft.luongchung.ftuschedule;


import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import pl.droidsonroids.gif.GifImageView;
import vn.lcsoft.luongchung.StaticCode;
import vn.lcsoft.luongchung.adapters.AdapterExpand;
import vn.lcsoft.luongchung.models.GroupEx;
import vn.lcsoft.luongchung.models.LichChuan;

import static vn.lcsoft.luongchung.StaticCode.DB_APP;
import static vn.lcsoft.luongchung.StaticCode.isAdsForConfig;
import static vn.lcsoft.luongchung.StaticCode.sf;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAll extends Fragment implements Comparator<LichChuan> {

    private GifImageView gifImageView;
    private RelativeLayout relativeLayout;
    private ExpandableListView listView;
    private ArrayList<GroupEx> listDataHeader;
    private HashMap<GroupEx, ArrayList<LichChuan>> listHash;
    private SQLiteDatabase sqLiteDatabase = null;
    private Date date_Min = new Date();
    private Date date_Max = new Date();
    private ArrayList<LichChuan> arr_tatca;
    private SharedPreferences sharedPreferences;
    private AdapterExpand adapterExpand = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);
        listView = view.findViewById(R.id.lv_tatca);
        listView.setFastScrollEnabled(true);
        sharedPreferences = getActivity().getSharedPreferences(DB_APP, MODE_PRIVATE);
        relativeLayout = view.findViewById(R.id.bk_image2);
        gifImageView = view.findViewById(R.id.gifall);
        arr_tatca = new ArrayList<>();
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        playGif();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        new initLoadDB().execute();
    }

    public void playGif() {
        final String url_qbee = "https://luongchung.github.io/qbee/a4.gif";
        final Animation fadeout = new AlphaAnimation(1.f, 1.f);
        fadeout.setDuration(4000); // You can modify the duration here
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Glide.with(getContext()).load(url_qbee).into(gifImageView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gifImageView.startAnimation(fadeout);
            }
        });
        gifImageView.startAnimation(fadeout);
    }

    private void getMaxMinOfDay() {
        try {
            date_Min = sf.parse("2099-01-01 00:00:00");
            date_Max = sf.parse("1996-01-01 00:00:00");
        } catch (ParseException e) {
        }
        for (int i = 0; i < arr_tatca.size(); i++) {
            if (date_Min.after(arr_tatca.get(i).getNgay())) date_Min = arr_tatca.get(i).getNgay();
            if (date_Max.before(arr_tatca.get(i).getNgay())) date_Max = arr_tatca.get(i).getNgay();
        }
    }
    @Override
    public int compare(LichChuan LichChuan, LichChuan t1) {
        if (Integer.parseInt(LichChuan.getTietBatDau()) > Integer.parseInt(t1.getTietBatDau()))
            return 1;
        if (Integer.parseInt(LichChuan.getTietBatDau()) == Integer.parseInt(t1.getTietBatDau()))
            return 0;
        return -1;
    }
    @SuppressLint("StaticFieldLeak")
    public class initLoadDB extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
            listHash.clear();
            listDataHeader.clear();
            arr_tatca.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loadDatabase();
            getMaxMinOfDay();
            handleArr();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    // Code here will run in UI thread
                    if (listDataHeader.size() == 0) {
                        relativeLayout.setVisibility(View.VISIBLE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(isAdsForConfig, false);
                        editor.commit();
                    } else {
                        relativeLayout.setVisibility(View.INVISIBLE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(isAdsForConfig, true);
                        editor.commit();
                    }


                    ArrayList<GroupEx> listDataHeaderX = new ArrayList<>(listDataHeader);
                    HashMap<GroupEx, ArrayList<LichChuan>> listHashX = new HashMap<>(listHash);
                    adapterExpand = new AdapterExpand(getActivity(), listDataHeaderX, listHashX);
                    listView.setAdapter(adapterExpand);
                    adapterExpand.notifyDataSetChanged();
                }
            });

        }
    }
    private void handleArr() {
        Calendar beginDay = Calendar.getInstance();
        Calendar endDay = Calendar.getInstance();
        beginDay.setTime(date_Min);
        endDay.setTime(date_Max);
        endDay.add(Calendar.DATE, 1);
        ArrayList<LichChuan> tmpArr;
        SimpleDateFormat sfShow = new SimpleDateFormat("dd/MM/yyyy");
        int idG = 0;
        while (beginDay.getTime().before(endDay.getTime())) {
            tmpArr = new ArrayList<>();
            String title;
            if (beginDay.get(Calendar.DAY_OF_WEEK) == 1) {
                title = "Chủ nhật" + ", Ngày " + sfShow.format(beginDay.getTime());
            } else {
                title = "Thứ " + beginDay.get(Calendar.DAY_OF_WEEK) + ", Ngày " + sfShow.format(beginDay.getTime());
            }
            GroupEx groupEx = new GroupEx(idG,title);
            listDataHeader.add(groupEx);
            for (int i = 0; i < arr_tatca.size(); i++) {
                if (beginDay.getTime().equals(arr_tatca.get(i).getNgay())){
                    tmpArr.add(arr_tatca.get(i));
                }

            }
            Collections.sort(tmpArr, new FragmentAll());
            for (int i = 0; i < tmpArr.size(); i++){
                tmpArr.get(i).setId(i);
            }
            Date date = new Date();
            if(tmpArr.size() == 0) tmpArr.add(new LichChuan(true,0,"Trong","Trong","","h",date,"2","1","3","3",""));
            listHash.put(groupEx, tmpArr);
            beginDay.add(Calendar.DATE, 1);
            idG++;
        }
    }
    private void loadDatabase() {
        Calendar cal = Calendar.getInstance();
        Date ngayhomnay = cal.getTime();
        SimpleDateFormat sfOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
        sqLiteDatabase = getActivity().openOrCreateDatabase(StaticCode.DB_NAME, Context.MODE_PRIVATE, null);
        String sql = "select * from tbthoikhoabieu where NgayHoc >= Datetime('" + sfOnlyDay.format(ngayhomnay) + " 00:00:00')";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        Date date = new Date();
        while (cursor.moveToNext()) {
            try {
                date = sf.parse(cursor.getString(5));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            arr_tatca.add(new LichChuan(false,Integer.parseInt(
                    cursor.getString(0)), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), date, cursor.getString(6),
                    cursor.getString(7), cursor.getString(8),
                    cursor.getString(9), cursor.getString(10)));
        }
        cursor.close();
    }
}
