package com.bo.android.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

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
        setHasOptionsMenu(true);
        setupThumbnailDownloader();
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
        updateItems(0);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
            searchView.setSearchableInfo(searchInfo);
        }
*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                search();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getSearchQuery() {
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return pm.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
    }

    private void search() {
        if ((getActivity().getResources().getConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK)
                != Configuration.UI_MODE_TYPE_TELEVISION) {

            getActivity().startSearch(getSearchQuery(), true, null, false);
        }
    }

    private void setupGrid(View view) {
        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(new EndlessScrollListener(ITEMS_PER_PAGE) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                updateItems(page);
            }
        });
    }

    private void setupProgressBar(View view) {
        progressContainer = view.findViewById(R.id.grid_view_progress_bar_container);
        progressContainer.setVisibility(View.INVISIBLE);
    }

    public void updateItems(int page) {
        new FetchItemsTask().execute(page, ITEMS_PER_PAGE);
    }

    private void setupThumbnailDownloader() {
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
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, FlickrItems> {

        @Override
        protected FlickrItems doInBackground(Integer... params) {
            Activity activity = getActivity();
            if (activity == null) {
                return new FlickrItems();
            }
            String query = getSearchQuery();
            if (query != null) {
                return new FlickrFetchr(getActivity()).search(query);
            } else {
                return new FlickrFetchr(getActivity()).fetchItems(params[0], ITEMS_PER_PAGE);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(FlickrItems items) {
            adapter.addAll(items.getItems());
            progressContainer.setVisibility(View.INVISIBLE);
            if (getSearchQuery() != null) {
                Toast.makeText(getActivity(), items.getTotal() + " items found", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(items);
        }

    }

/*
    private class SearchTask extends AsyncTask<Integer, Void, FlickrItems> {

        @Override
        protected FlickrItems doInBackground(Integer... params) {
            Activity activity = getActivity();
            if (activity != null) {
                String query = getSearchQuery();
                if (query != null) {
                    return new FlickrFetchr(getActivity()).search(query);
                }
            }
            return new FlickrItems();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(FlickrItems items) {
            adapter.addAll(items.getItems());
            progressContainer.setVisibility(View.INVISIBLE);
            super.onPostExecute(items);
        }

    }
*/

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
            imageView.setImageResource(R.mipmap.ic_launcher);
            GalleryItem item = getItem(position);
            thumbnailDownloader.queueThumbnail(imageView, item.getUrl());

            return view;
        }
    }

}
