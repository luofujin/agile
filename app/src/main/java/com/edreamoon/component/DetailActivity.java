package com.edreamoon.component;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.edreamoon.router.annotation.*;


@Router(path = "app/detail")
public class DetailActivity extends AppCompatActivity {

    String mName;
    String mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        new Test();
//        new FRouter();
//        FRouter.inject(this);
//        PhotoActivity
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, DetailActivity.class);
        context.startActivity(starter);
    }
}


class DetailActivity$A {

    public void inject(DetailActivity activity) {
        Intent intent = activity.getIntent();
        activity.mName = intent.getStringExtra("name");
        activity.mAuthor = intent.getStringExtra("author");
    }
}
