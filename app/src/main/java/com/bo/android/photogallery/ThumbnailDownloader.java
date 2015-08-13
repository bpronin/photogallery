package com.bo.android.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler handler;
    private Map<T, String> requestMap = Collections.synchronizedMap(new HashMap<T, String>());
    private Listener<T> listener;
    private Handler responseHandler;
    private Context context;

    public ThumbnailDownloader(Context context, Handler responseHandler) {
        super(TAG);
        this.context = context;
        this.responseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T token = (T) msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(T token, String url) {
        Log.i(TAG, "Got a URL: " + url);
        requestMap.put(token, url);
        handler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    private void handleRequest(final T token) {
        try {
            final String url = requestMap.get(token);
            if (url != null) {
                byte[] bitmapBytes = new FlickrFetchr(context).getUrlBytes(url);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

                Log.i(TAG, "Bitmap created");

                responseHandler.post(new Runnable() {

                    public void run() {
                        String url2 = requestMap.get(token);
                        if (url2 != null && url2.equals(url)) {
                            requestMap.remove(token);
                            listener.onThumbnailDownloaded(token, bitmap);
                        }
                    }
                });
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void clearQueue() {
        handler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    public static abstract class Listener<T> {

        abstract void onThumbnailDownloaded(T token, Bitmap thumbnail);

    }
}