package com.bo.android.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private static final int ITEMS_PER_PAGE = 100;

    private PhotoGalleryGridAdapter adapter;
    private View progressContainer;
    private ThumbnailDownloader<ImageView> thumbnailDownloader;

    public PhotoGalleryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        thumbnailDownloader = new ThumbnailDownloader<>(getActivity(), new Handler());
        thumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {

            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        thumbnailDownloader.start();
        thumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

        adapter = new PhotoGalleryGridAdapter(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        setupProgressBar(view);
        setupGrid(view);
        loadItems(0);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }
    private void setupGrid(View view) {
        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(new EndlessScrollListener(ITEMS_PER_PAGE) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadItems(page);
            }
        });
    }

    private void setupProgressBar(View view) {
        progressContainer = view.findViewById(R.id.grid_view_progress_bar_container);
        progressContainer.setVisibility(View.INVISIBLE);
    }

    private void loadItems(int page) {
        new FetchItemsTask().execute(page, ITEMS_PER_PAGE);
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr(getActivity()).fetchItems(params[0], ITEMS_PER_PAGE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            adapter.addAll(items);
            progressContainer.setVisibility(View.INVISIBLE);
            super.onPostExecute(items);
        }

    }

    private class PhotoGalleryGridAdapter extends ArrayAdapter<GalleryItem> {

        public PhotoGalleryGridAdapter(Activity activity) {
            super(activity, 0, new ArrayList<GalleryItem>());
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.fragment_photo_gallery_item, parent, false);
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.gallery_item_image_view);
            imageView.setImageResource(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            GalleryItem item = getItem(position);
            thumbnailDownloader.queueThumbnail(imageView, item.getUrl());

            return view;
        }
    }

}
