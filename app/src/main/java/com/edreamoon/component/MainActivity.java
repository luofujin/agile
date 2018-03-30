package com.edreamoon.component;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

//import com.edreamoon.photo.PhotoActivity;
//import com.edreamoon.mread.ReadMainActivity;


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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.detail) {
           DetailActivity.start(this);
        }

//        else if (id == R.id.read) {
//            ReadMainActivity.start(this);
//        }
    }
}