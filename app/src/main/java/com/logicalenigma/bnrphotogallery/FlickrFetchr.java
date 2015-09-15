package com.logicalenigma.bnrphotogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Logical Enigma
 * Created by dnachman on 9/11/15.
 */
public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "58b603943a04c8e11fb385ad7d1efd5e";
    private static final String BASE_URL = "https://api.flickr.com/services/rest/";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " : with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems() {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);

            parseItems(items, jsonBody);

        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject jsonPhotos = jsonBody.getJSONObject("photos");
        JSONArray jsonPhotoArray = jsonPhotos.getJSONArray("photo");

        for (int i = 0; i < jsonPhotoArray.length(); i++) {
            JSONObject jsonPhoto = jsonPhotoArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();

            item.setId(jsonPhoto.getString("id"));
            item.setCaption(jsonPhoto.getString("title"));

            if (!jsonPhoto.has("url_s")) {
                continue;
            }

            item.setUrl(jsonPhoto.getString("url_s"));
            items.add(item);

        }
    }
}
