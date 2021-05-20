package com.example.ontheleash;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MapSettings {
    private static MapSettings instance;

    public static synchronized MapSettings getInstance(Context context) {
        if (instance == null)
            instance = new MapSettings(context);
        return instance;
    }

    public enum MapStyle{
        NORMAL,
        SATELLITE,
        HYBRID
    }

    private MapSettings(Context context){
        SharedPreferences mSettings;
        mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

        String mapSettingsJson = mSettings.getString(Settings.APP_PREFERENCES_MAP_SETTINGS_JSON, "");
        // if shared prefs has mapSettings
        if (!mapSettingsJson.equals("")) {
            Gson g = new Gson();
            MapSettings mapSettings = g.fromJson(mapSettingsJson, MapSettings.class);

            this.mapStyle = mapSettings.mapStyle;
            this.noDogsAreas = mapSettings.noDogsAreas;
            this.markerTypeList = mapSettings.markerTypeList;
            this.male = mapSettings.male;
            this.female = mapSettings.female;
            this.birthday = mapSettings.birthday;
            this.castration = mapSettings.castration;
            this.ready_to_mate = mapSettings.ready_to_mate;
            this.for_sale = mapSettings.for_sale;
            this.breeds = mapSettings.breeds;
        }
        // if shared prefs does not have mapSettings
        else{
            // only dog markers
            markerTypeList.add(MyClusteringItem.MarkerType.DOG);
        }
    }

    public MapStyle mapStyle = MapStyle.NORMAL;
    public boolean noDogsAreas = false;
    public List<MyClusteringItem.MarkerType> markerTypeList = new ArrayList<>();
    public boolean male = true;
    public boolean female = true;
    public String birthday = "";
    public boolean castration = false;
    public boolean ready_to_mate = false;
    public boolean for_sale = false;
    public List<String> breeds = new ArrayList<>();

    public void update(Context context) {
        Gson g = new Gson();
        String json = g.toJson(this);

        SharedPreferences mSettings;
        mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(Settings.APP_PREFERENCES_MAP_SETTINGS_JSON, json);
        editor.apply();
    }
}
