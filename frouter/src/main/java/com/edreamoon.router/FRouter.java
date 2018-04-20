package com.edreamoon.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

public class FRouter {

    private Context mContext;
    private static final HashMap<String, String> ACTIVITIES = new HashMap<>();


    public void init(Context context) {
        mContext = context;
    }

    public void start(String path) {
        Intent intent = new Intent();
        String ac = ACTIVITIES.get(path);
        ComponentName component = new ComponentName(mContext.getPackageName(), ac);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(component);
        mContext.startActivity(intent);
    }

    public String getActivity(String key) {
        return ACTIVITIES.get(key);
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
