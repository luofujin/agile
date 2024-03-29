package com.edreamoon.component;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.edreamoon.router.FRouter;

/**
 * Created by jianfeng.li on 2017/11/24.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.photo).setOnClickListener(this);
        findViewById(R.id.read).setOnClickListener(this);
        findViewById(R.id.detail).setOnClickListener(this);
        init();
    }

    private void init() {
        Log.e("lijf", "init: " + FRouter.instance().getActivity("app/third"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.detail) {
            FRouter.instance().start("photo/main");
        }

//        else if (id == R.id.read) {
//            ReadMainActivity.start(this);
//        }
    }
}