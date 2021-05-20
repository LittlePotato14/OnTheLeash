package com.example.ontheleash;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import org.jetbrains.annotations.NotNull;

public class MyClusteringItem implements ClusterItem {
    protected double latitude;
    protected double longitude;
    private final String title;
    private final String snippet;

    protected MarkerOptions markerOptions;

    public MyClusteringItem(double lat, double lng, String title, String snippet) {
        this.latitude = lat;
        this.longitude = lng;
        this.title = title;
        this.snippet = snippet;
    }

    private int id;

    public enum MarkerType{
        @SerializedName("0")
        DOG,
        @SerializedName("1")
        DOG_PARK,
        @SerializedName("2")
        DOG_FRIENDLY,
        @SerializedName("3")
        HOSPITAL,
        @SerializedName("4")
        SHELTER,
        @SerializedName("5")
        STORE,
        @SerializedName("6")
        HOTEL,
        @SerializedName("7")
        TRAINING,
        @SerializedName("8")
        BREEDING
    }

    private MarkerType markerType;

    public MarkerType getMarkerType() {
        return this.markerType;
    }

    public int getId() {
        return id;
    }

    @NotNull
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public MarkerOptions getMarker() {
        return markerOptions;
    }

    public void setMarker(MarkerOptions marker) {
        this.markerOptions = marker;
    }
}