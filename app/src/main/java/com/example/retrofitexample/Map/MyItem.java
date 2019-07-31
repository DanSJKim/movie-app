package com.example.retrofitexample.Map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private final String mTitle;
    private final String mSnippet;
    public final int profilePhoto;

    public MyItem(double lat, double lng, String t, String s, int pictureResource) {
        mPosition = new LatLng(lat, lng);
        mTitle = t;
        mSnippet = s;
        profilePhoto = pictureResource;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getSnippet(){
        return mSnippet;
    }
}