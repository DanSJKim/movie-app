package com.example.retrofitexample.Map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Theater implements ClusterItem {

    private int theaterid;
    private String theaterName;
    private String theaterAddress;
    private String theaterStreetAddress;
    private String theaterTelNum;
    private String theaterLatitude;
    private String theaterLongitude;
    private String theaterUrl;
    private LatLng location;

    public Theater(double lat, double lng) {
        location = new LatLng(lat, lng);
    }

    public int getTheaterid() {
        return theaterid;
    }

    public void setTheaterid(int theaterid) {
        this.theaterid = theaterid;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getTheaterAddress() {
        return theaterAddress;
    }

    public void setTheaterAddress(String theaterAddress) {
        this.theaterAddress = theaterAddress;
    }

    public String getTheaterStreetAddress() {
        return theaterStreetAddress;
    }

    public void setTheaterStreetAddress(String theaterStreetAddress) {
        this.theaterStreetAddress = theaterStreetAddress;
    }

    public String getTheaterTelNum() {
        return theaterTelNum;
    }

    public void setTheaterTelNum(String theaterTelNum) {
        this.theaterTelNum = theaterTelNum;
    }

    public String getTheaterLatitude() {
        return theaterLatitude;
    }

    public void setTheaterLatitude(String theaterLatitude) {
        this.theaterLatitude = theaterLatitude;
    }

    public String getTheaterLongitude() {
        return theaterLongitude;
    }

    public void setTheaterLongitude(String theaterLongitude) {
        this.theaterLongitude = theaterLongitude;
    }

    public String getTheaterUrl() {
        return theaterUrl;
    }

    public void setTheaterUrl(String theaterUrl) {
        this.theaterUrl = theaterUrl;
    }

    @Override
    public LatLng getPosition() {
        return location;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
