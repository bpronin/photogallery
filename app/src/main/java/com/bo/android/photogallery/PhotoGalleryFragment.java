package com.bo.android.photogallery;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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

    public PhotoGalleryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

    private void setupGrid(View view) {
        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        adapter = new PhotoGalleryGridAdapter(getActivity());
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

        public PhotoGalleryGridAdapter(FragmentActivity activity) {
            super(activity, android.R.layout.simple_gallery_item, new ArrayList<GalleryItem>());
        }

    }

}
