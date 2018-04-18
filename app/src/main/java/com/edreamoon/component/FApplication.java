package com.edreamoon.component;

import android.app.Application;

import com.edreamoon.router.FRouter;


public class FApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FRouter.instance().init(this);
    }
}
