package vn.lcsoft.luongchung.ftuschedule;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyHttpRequest {
    private static MyHttpRequest instance;
    private RequestQueue requestQueue;
    private static Context ctx;
    private MyHttpRequest(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }
    public static synchronized MyHttpRequest getInstance(Context context) {
        if (instance == null) instance = new MyHttpRequest(context);
        return instance;
    }
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        return requestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
