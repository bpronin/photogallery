package com.bo.android.photogallery;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
class BitmapCache {

    private static final boolean VERSION_VALID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;

    private LruCache<String, Bitmap> cache;

    public BitmapCache() {
        if (VERSION_VALID) {
            cache = new LruCache<>(100);
        }
    }

    public Bitmap get(String url) {
        return VERSION_VALID ? cache.get(url) : null;
    }

    public void put(String url, Bitmap bitmap) {
        if (VERSION_VALID) {
            cache.put(url, bitmap);
        }
    }
}
