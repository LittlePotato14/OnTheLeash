package com.example.ontheleash.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ontheleash.MapSettings;
import com.example.ontheleash.MyClusteringItem;
import com.example.ontheleash.OwnIconRendered;
import com.example.ontheleash.R;
import com.example.ontheleash.activities.MainActivity;
import com.example.ontheleash.activities.MapFiltersActivity;
import com.example.ontheleash.api.ApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;


public class MapFragment extends Fragment {
    GoogleMap map;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 90;
    private final int GPS_REQUEST_CODE = 2;
    FusedLocationProviderClient fusedLocationProviderClient;
    MarkerOptions searchLocation;
    Geocoder geocoder;

    BitmapDescriptor dogFriendlyMarker;
    BitmapDescriptor breedingMarker;
    BitmapDescriptor dogTrainingMarker;
    BitmapDescriptor storeMarker;
    BitmapDescriptor shelterMarker;
    BitmapDescriptor hotelMarker;
    BitmapDescriptor hospitalMarker;
    BitmapDescriptor parkMarker;
    BitmapDescriptor dogMarker;

    // Declare a variable for the cluster manager.
    private ClusterManager<MyClusteringItem> clusterManager;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        if(map != null) {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            getMarkersApi(bounds.northeast.latitude, bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.longitude);

            switch (MapSettings.getInstance(getActivity()).mapStyle){
                case HYBRID:
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case NORMAL:
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case SATELLITE:
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
            }
        }

        super.onResume();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        geocoder = new Geocoder(getContext());

        createMarkerIcons();

        // initialize map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // async map
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                switch (MapSettings.getInstance(getActivity()).mapStyle) {
                    case HYBRID:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case NORMAL:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case SATELLITE:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }

                // clusters
                clusterManager = new ClusterManager<MyClusteringItem>(getActivity(), map);
                clusterManager.setRenderer(new OwnIconRendered(
                        getActivity(), map, clusterManager));
                map.setOnCameraIdleListener(clusterManager);
                map.setOnMarkerClickListener(clusterManager);

                clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyClusteringItem>(){

                    @Override
                    public boolean onClusterItemClick(MyClusteringItem item) {
                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager) (getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                        // open bottom sheet
                        ((MainActivity)getActivity()).openBottomSheet(item.getId(), item.getMarkerType());

                        return true;
                    }
                });

                clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyClusteringItem>(){

                    @Override
                    public boolean onClusterClick(Cluster<MyClusteringItem> cluster) {
                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager) (getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                        // zoom in cluster
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (MyClusteringItem marker : cluster.getItems())
                            builder.include(marker.getPosition());
                        LatLngBounds bounds = builder.build();

                        int padding = 30; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        map.animateCamera(cu);

                        return true;
                    }
                });

                map.setIndoorEnabled(true);
                // remove all POIs
                map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getContext(), R.raw.map_style_json));

                // end of moving
                map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                        getMarkersApi(bounds.northeast.latitude, bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.longitude);
                    }
                });

                map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            return false;
                        EnableGPS();
                        return true;
                    }
                });

                // change positions of default views
                if (getView().findViewById(Integer.parseInt("1")) != null) {
                    // Location button
                    View locationButton = ((View) getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                    RelativeLayout.LayoutParams locationLayoutParams = (RelativeLayout.LayoutParams)
                            locationButton.getLayoutParams();
                    locationLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    locationLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    locationLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                    locationLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
                    locationLayoutParams.leftMargin = 30;
                    locationLayoutParams.bottomMargin = 110;

                    // Compass button
                    View compassButton = ((View) getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("5"));
                    RelativeLayout.LayoutParams compassLayoutParams = (RelativeLayout.LayoutParams)
                            compassButton.getLayoutParams();
                    compassLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    compassLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    compassLayoutParams.rightMargin = compassLayoutParams.leftMargin;
                    compassLayoutParams.bottomMargin = 260;
                }

                setupPermissions();

                /*map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        MarkerOptions mo = new MarkerOptions();
                        mo.position(latLng);
                        mo.title(latLng.latitude + ":" + latLng.longitude);
                        map.clear();
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        map.addMarker(mo);
                    }
                });*/

                //LatLng moscow = new LatLng(55.751688, 37.616791);
                //map.addMarker(new MarkerOptions().position(moscow).title("Wohooooooo"));
                //map.moveCamera(CameraUpdateFactory.newLatLng(moscow));
            }
        });

        return view;
    }

    // cause we cannot just pass layer-list xml to marker icon drawable
    private void createMarkerIcons(){
        Drawable drawable = getResources().getDrawable(R.drawable.dog_frienly_marker_icon);
        Bitmap btm = drawableToBitmap (drawable);
        Bitmap smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        dogFriendlyMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.breeding_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        breedingMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.dog_training_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        dogTrainingMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.store_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        storeMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.shelter_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        shelterMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.hotel_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        hotelMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.hospital_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        hospitalMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.park_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        parkMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);

        drawable = getResources().getDrawable(R.drawable.dog_marker_icon);
        btm = drawableToBitmap (drawable);
        smallMarker = Bitmap.createScaledBitmap(btm, 100, 100, false);
        dogMarker = BitmapDescriptorFactory
                .fromBitmap(smallMarker);
    }

    private String myJoin(String sep, List<String> list){
        StringBuilder sb = new StringBuilder();
        if(list.size() > 0)
            sb.append(list.get(0));
        for(int i = 1; i < list.size(); i++)
            sb.append(sep).append(list.get(i));
        return sb.toString();
    }

    private void getMarkersApi(double latitude_t, double latitude_b, double longitude_l, double longitude_r) {
        MapSettings mapSettings = MapSettings.getInstance(getContext());

        ApiClient.getInstance()
                .getApi()
                .getMarkers(latitude_t, latitude_b, longitude_l, longitude_r,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.DOG) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.DOG_PARK) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.DOG_FRIENDLY) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.HOSPITAL) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.SHELTER) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.STORE) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.HOTEL) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.TRAINING) ? 1 : 0,
                        mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.BREEDING) ? 1 : 0,

                        mapSettings.male ? 1 : 0,
                        mapSettings.female ? 1 : 0,
                        mapSettings.castration ? 1 : 0,
                        mapSettings.ready_to_mate ? 1 : 0,
                        mapSettings.for_sale ? 1 : 0,
                        mapSettings.birthday,
                        myJoin("|", mapSettings.breeds)
                        )
                .enqueue(new Callback<List<MyClusteringItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<MyClusteringItem>> call, @NonNull Response<List<MyClusteringItem>> response) {
                        if (response.isSuccessful()) {
                            //map.clear();
                            clusterManager.clearItems();
                            List<MyClusteringItem> markers = response.body();
                            if(markers != null) {
                                for (int i = 0; i < markers.size(); i++) {
                                    MarkerOptions mo = new MarkerOptions()
                                                    .position(new LatLng(markers.get(i).getPosition().latitude, markers.get(i).getPosition().longitude))
                                                    .title(markers.get(i).getMarkerType().toString());

                                    // set different marker icons depend on marker type
                                    switch (markers.get(i).getMarkerType()){
                                        case DOG:
                                            mo.icon(dogMarker);
                                            break;
                                        case HOTEL:
                                            mo.icon(hotelMarker);
                                            break;
                                        case STORE:
                                            mo.icon(storeMarker);
                                            break;
                                        case SHELTER:
                                            mo.icon(shelterMarker);
                                            break;
                                        case BREEDING:
                                            mo.icon(breedingMarker);
                                            break;
                                        case DOG_PARK:
                                            mo.icon(parkMarker);
                                            break;
                                        case HOSPITAL:
                                            mo.icon(hospitalMarker);
                                            break;
                                        case TRAINING:
                                            mo.icon(dogTrainingMarker);
                                            break;
                                        case DOG_FRIENDLY:
                                            mo.icon(dogFriendlyMarker);
                                            break;
                                    }

                                    markers.get(i).setMarker(mo);

                                    clusterManager.addItem(markers.get(i));
                                }

                                clusterManager.cluster();
                            }

                            // if user had searched any location
                            if(searchLocation != null)
                                map.addMarker(searchLocation);
                        } else {
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(getContext(), "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<MyClusteringItem>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        map.setMyLocationEnabled(true);
    }

    private void setupPermissions() {
        int permission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED)
            makeRequest();
        else {
            enableUserLocation();
            zoomToUsersLocation();
        }
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /* override it in parent activity
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        }
    }*/

    private void EnableGPS() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("GPS")
                    .setMessage("To continue, turn on device location")
                    .setPositiveButton("Ok", ((dialogInterface, i) -> {
                        Intent intent = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    }))
                    .setNegativeButton("No thanks", ((dialog, which) -> dialog.dismiss()))
                    .setCancelable(true)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (requestCode == GPS_REQUEST_CODE && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Toast.makeText(getContext(), "GPS is not enable", Toast.LENGTH_SHORT).show();
    }

    public void zoomToUsersLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });
    }

    public void showAddress(LatLng latLng, String title){
        searchLocation = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        map.clear();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        map.addMarker(searchLocation);
    }
}