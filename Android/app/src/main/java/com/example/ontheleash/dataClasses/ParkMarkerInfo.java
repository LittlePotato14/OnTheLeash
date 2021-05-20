package com.example.ontheleash.dataClasses;

public class ParkMarkerInfo {
    private double latitude;
    private double longitude;
    private int id;
    private int area;
    private int lighting;
    private int fencing;
    private String working_hours;
    private String elements;
    private String image;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public int getArea() {
        return area;
    }

    public int getLighting() {
        return lighting;
    }

    public int getFencing() {
        return fencing;
    }

    public String getWorking_hours() {
        return working_hours;
    }

    public String getImage() {
        return image;
    }

    public String getElements() {
        return elements;
    }
}
