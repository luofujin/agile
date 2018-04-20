package com.edreamoon.photo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.edreamoon.router.annotation.Router;


@Router(path = "photo/main")
public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecActivity.start(PhotoActivity.this);
            }
        });

        findViewById(R.id.bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ReadMainActivity.start(PhotoActivity.this);
            }
        });
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, PhotoActivity.class));
    }
}
