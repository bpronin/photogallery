package com.bo.android.photogallery;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class PhotoPageActivity extends FragmentActivity {

    private static final String TAG = "PhotoPageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_page);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        PhotoPageFragment fragment = (PhotoPageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

}
