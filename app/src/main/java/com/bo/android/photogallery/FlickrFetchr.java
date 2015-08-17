package com.bo.android.photogallery;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    public static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    public static final String PARAM_METHOD = "method";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PER_PAGE = "per_page";
    public static final String PARAM_API_KEY = "api_key";
    public static final String EXTRA_SMALL_URL = "url_s";
    private static final String PARAM_EXTRAS = "extras";
    private static final String PARAM_TEXT = "text";

    private final String apiKey;

    public FlickrFetchr(Context context) {
        apiKey = context.getString(R.string.frikr_api_key);
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return new byte[0];
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(int page, int perPage) {
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter(PARAM_METHOD, "flickr.photos.getRecent")
                .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage))
                .appendQueryParameter(PARAM_API_KEY, apiKey)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();
        return downloadItems(url);
    }

    public List<GalleryItem> search(String query) {
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter(PARAM_METHOD, "flickr.photos.search")
                .appendQueryParameter(PARAM_API_KEY, apiKey)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT, query)
                .build().toString();
        return downloadItems(url);
    }

    private List<GalleryItem> downloadItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String xmlString = getUrl(url);
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseItems(items, parser);
        } catch (Exception x) {
            Log.e(TAG, "Failed to fetch items", x);
        }
        return items;
    }

    private void parseItems(List<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.next();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && "photo".equals(parser.getName())) {
                GalleryItem item = new GalleryItem();
                item.setId(parser.getAttributeValue(null, "id"));
                item.setCaption(parser.getAttributeValue(null, "title"));
                item.setUrl(parser.getAttributeValue(null, "url_s"));
                items.add(item);
            }
            eventType = parser.next();
        }
    }
}
