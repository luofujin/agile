package com.edreamoon.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

public class FRouter {

    private Context mContext;
    private static final HashMap<String, String> ACTIVITIES = new HashMap<>();

    public void setData() {
        ACTIVITIES.put("app/detail", "com.edreamoon.DailyActivity");
        ACTIVITIES.put("app/share", "com.edreamoon.ShareActivity");
        Log.e("lijf", "setData: " + ACTIVITIES.get("abc"));
    }


    public void init(Context context) {
        mContext = context;
        setData();
    }

    public void start(String path) {
        Intent intent = new Intent();
        String ac = ACTIVITIES.get(path);
        ComponentName component = new ComponentName(mContext.getPackageName(), ac);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(component);
        mContext.startActivity(intent);
    }

    private FRouter() {
    }

    public static final FRouter instance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        public static final FRouter INSTANCE = new FRouter();
    }
}
