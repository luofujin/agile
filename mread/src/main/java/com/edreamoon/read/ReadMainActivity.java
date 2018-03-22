package com.edreamoon.read;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ReadMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_main);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ReadMainActivity.class);
        context.startActivity(intent);
    }
}
