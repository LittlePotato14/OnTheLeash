package com.example.ontheleash;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class OwnIconRendered extends DefaultClusterRenderer<MyClusteringItem> {

    public OwnIconRendered(Context context, GoogleMap map,
                           ClusterManager<MyClusteringItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MyClusteringItem item,
                                               MarkerOptions markerOptions) {
        markerOptions.icon(item.getMarker().getIcon());
    }

}