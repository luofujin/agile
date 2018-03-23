package com.edreamoon.component;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {

    String mName;
    String mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
//        FRouter.inject(this);
    }
}

class DetailActivity$ {

    public void inject(DetailActivity activity) {
        Intent intent = activity.getIntent();
        activity.mName = intent.getStringExtra("name");
        activity.mAuthor = intent.getStringExtra("author");
    }
}
