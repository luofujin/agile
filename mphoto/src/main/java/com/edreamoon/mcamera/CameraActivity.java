package com.edreamoon.mcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

//import com.edreamoon.mread.ReadMainActivity;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_camera);

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecActivity.start(CameraActivity.this);
            }
        });

        findViewById(R.id.bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ReadMainActivity.start(CameraActivity.this);
            }
        });
    }
}
