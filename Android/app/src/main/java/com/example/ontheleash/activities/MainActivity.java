package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.ontheleash.Fragments.MapFragment;
import com.example.ontheleash.MyClusteringItem;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.jsonResponse;
import com.example.ontheleash.dataClasses.DogMarkerInfo;
import com.example.ontheleash.dataClasses.ParkMarkerInfo;
import com.example.ontheleash.dataClasses.PlaceMarkerInfo;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ImageButton topButton;
    private SearchView search;
    private Geocoder geocoder;
    private NavHostFragment navHostFragment;
    private NavController navController;

    private BottomSheetBehavior dogBottomSheetBehavior;
    private ConstraintLayout dogBottomSheetLayout;
    private BottomSheetBehavior placesBottomSheetBehavior;
    private ConstraintLayout placesBottomSheetLayout;
    private boolean scrollablePlacesBottomSheet = true;
    private BottomSheetBehavior parksBottomSheetBehavior;
    private ConstraintLayout parksBottomSheetLayout;
    private boolean scrollableParksBottomSheet = true;

    private TextView detailsText;
    private ImageButton expandButton;
    GridLayout hoursGrid;
    ImageButton expandButton2;

    private TextView elementsText;
    private ImageButton expandButtonParks;
    GridLayout hoursGridParks;
    ImageButton expandButton2Parks;

    public int user_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove splash screen
        setTheme(R.style.Theme_OnTheLeash);

        SharedPreferences mSettings;
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

        // first time in app => open welcome activity
        if (mSettings.getBoolean(Settings.APP_PREFERENCES_FIRST_IN, true)) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);

        // make settings visible if user is logged in
        NavigationView navigationView = findViewById(R.id.navView);
        Menu menu = navigationView.getMenu();
        MenuItem targetSettings = menu.findItem(R.id.settings);

        targetSettings.setVisible(mSettings.getBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false));

        // ToDo uncomment when messages will be done
        // make messages visible if user is logged in
        /*MenuItem targetMessages = menu.findItem(R.id.messages);

        targetMessages.setVisible(mSettings.getBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false));*/

        dogBottomSheetLayout = findViewById(R.id.bottom_sheet_dog_layout);
        dogBottomSheetBehavior = BottomSheetBehavior.from(dogBottomSheetLayout);
        dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        parksBottomSheetLayout = findViewById(R.id.bottom_sheet_parks_layout);

        // make expandable elements
        elementsText = findViewById(R.id.elements_text_view);
        expandButtonParks = findViewById(R.id.expand_elements_button);

        expandButtonParks.setOnClickListener(v -> {
            if(elementsText.getVisibility() == View.GONE){
                elementsText.setVisibility(View.VISIBLE);
                expandButtonParks.setImageResource(R.drawable.up_icon_green);
            }else{
                elementsText.setVisibility(View.GONE);
                expandButtonParks.setImageResource(R.drawable.down_icon_green);
            }
        });

        // make expandable hours
        hoursGridParks = findViewById(R.id.hours_grid_parks);
        expandButton2Parks = findViewById(R.id.expand_hours_button_parks);

        expandButton2Parks.setOnClickListener(v -> {
            if(hoursGridParks.getVisibility() == View.GONE){
                hoursGridParks.setVisibility(View.VISIBLE);
                expandButton2.setImageResource(R.drawable.up_icon_green);
            }else{
                hoursGridParks.setVisibility(View.GONE);
                expandButton2Parks.setImageResource(R.drawable.down_icon_green);
            }
        });

        ScrollView scroll = parksBottomSheetLayout.findViewById(R.id.scrollView);
        // now we can scroll to the top without hiding
        scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                scrollableParksBottomSheet = (scroll.getScrollY() == 0);
            }
        });

        parksBottomSheetBehavior = BottomSheetBehavior.from(parksBottomSheetLayout);
        parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        parksBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_DRAGGING && !scrollableParksBottomSheet)
                    parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


        placesBottomSheetLayout = findViewById(R.id.bottom_sheet_places_layout);

        // make expandable details
        detailsText = findViewById(R.id.details_text_view);
        expandButton = findViewById(R.id.expand_details_button);

        expandButton.setOnClickListener(v -> {
            if(detailsText.getVisibility() == View.GONE){
                detailsText.setVisibility(View.VISIBLE);
                expandButton.setImageResource(R.drawable.up_icon_green);
            }else{
                detailsText.setVisibility(View.GONE);
                expandButton.setImageResource(R.drawable.down_icon_green);
            }
        });

        // make expandable hours
        hoursGrid = findViewById(R.id.hours_grid);
        expandButton2 = findViewById(R.id.expand_hours_button);

        expandButton2.setOnClickListener(v -> {
            if(hoursGrid.getVisibility() == View.GONE){
                hoursGrid.setVisibility(View.VISIBLE);
                expandButton2.setImageResource(R.drawable.up_icon_green);
            }else{
                hoursGrid.setVisibility(View.GONE);
                expandButton2.setImageResource(R.drawable.down_icon_green);
            }
        });

        ScrollView scroll1 = placesBottomSheetLayout.findViewById(R.id.scrollView);
        // now we can scroll to the top without hiding
        scroll1.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                scrollablePlacesBottomSheet = (scroll1.getScrollY() == 0);
            }
        });

        placesBottomSheetBehavior = BottomSheetBehavior.from(placesBottomSheetLayout);
        placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        placesBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_DRAGGING && !scrollablePlacesBottomSheet)
                    placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        topButton = findViewById(R.id.topButton);
        topButton.setEnabled(false);
        topButton.setVisibility(View.INVISIBLE);

        search = findViewById(R.id.search);
        search.setEnabled(false);
        search.setVisibility(View.INVISIBLE);

        geocoder = new Geocoder(this);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = search.getQuery().toString();
                List<Address> addressList = null;

                if (!location.isEmpty()) {
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                        if (addressList.size() == 0) {
                            Toast.makeText(MainActivity.this, "No results", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        ((MapFragment) (navHostFragment.getChildFragmentManager().getFragments().get(0))).showAddress(latLng, address.getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return false;
            }
        });

        // connect menu icon to a drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(search.getApplicationWindowToken(), 0);
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // in other way icon are black
        navigationView.setItemIconTintList(null);

        // set controller
        navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        //region logout dialog
        Dialog logoutDialog = new Dialog(MainActivity.this);
        logoutDialog.setContentView(R.layout.dialog_logout);
        logoutDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dialog_bg));
        logoutDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView no = logoutDialog.findViewById(R.id.no);
        TextView yes = logoutDialog.findViewById(R.id.yes);

        no.setOnClickListener(v1 -> logoutDialog.dismiss());

        yes.setOnClickListener(v1 -> {
            // logout shared pref
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
            editor.apply();

            //server logout
            String jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");
            ApiClient.getInstance()
                    .getApi()
                    .logout(jwt)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                // refresh profile fragment
                                navController.navigate(R.id.action_profile_to_profile);
                                // make settings invisible
                                targetSettings.setVisible(false);
                                // ToDo uncomment when messages will be done
                                //targetMessages.setVisible(false);
                            } else {
                                switch (response.code()) {
                                    case 401:
                                        SharedPreferences mSettings;
                                        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = mSettings.edit();
                                        editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
                                        editor.apply();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                        break;
                                    case 500:
                                        Toast.makeText(MainActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Toast.makeText(MainActivity.this,
                                    "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });

            // close dialog
            logoutDialog.dismiss();
        });
        //endregion

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {

                SharedPreferences mSettings;
                mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

                switch (destination.getId()) {
                    case R.id.map:
                        topButton.setEnabled(true);
                        topButton.setVisibility(View.VISIBLE);
                        topButton.setImageResource(R.drawable.map_settings_icon_white);
                        topButton.setOnClickListener(v -> {
                            // hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(search.getApplicationWindowToken(), 0);

                            placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                            Intent intent = new Intent(MainActivity.this, MapFiltersActivity.class);
                            startActivity(intent);
                        });

                        search.setEnabled(true);
                        search.setVisibility(View.VISIBLE);
                        break;
                    case R.id.messages:
                    case R.id.support:
                    case R.id.settings:
                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(search.getApplicationWindowToken(), 0);

                        placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        topButton.setEnabled(false);
                        topButton.setVisibility(View.INVISIBLE);

                        search.setEnabled(false);
                        search.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.profile:
                        // hide keyboard
                        InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm1.hideSoftInputFromWindow(search.getApplicationWindowToken(), 0);

                        placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        if (user_id == -1 && mSettings.getBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false)) {
                            topButton.setEnabled(true);
                            topButton.setVisibility(View.VISIBLE);
                            topButton.setImageResource(R.drawable.logout_icon_white);
                            topButton.setOnClickListener(v -> logoutDialog.show());
                        } else {
                            topButton.setEnabled(false);
                            topButton.setVisibility(View.INVISIBLE);
                        }

                        search.setEnabled(false);
                        search.setVisibility(View.INVISIBLE);

                        break;
                }
            }
        });
    }

    /**
     * when user agrees the permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MapFragment.LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Need location permission", Toast.LENGTH_SHORT);
            else{
                ((MapFragment)(navHostFragment.getChildFragmentManager().getFragments().get(0))).enableUserLocation();
                ((MapFragment)(navHostFragment.getChildFragmentManager().getFragments().get(0))).zoomToUsersLocation();
            }
        }
    }

    public void openBottomSheet(int id, MyClusteringItem.MarkerType markerType){
        hoursGrid.setVisibility(View.GONE);
        expandButton2.setImageResource(R.drawable.down_icon_green);
        detailsText.setVisibility(View.GONE);
        expandButton.setImageResource(R.drawable.down_icon_green);

        placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if(markerType == MyClusteringItem.MarkerType.DOG)
            openDogSheet(id);
        else if(markerType == MyClusteringItem.MarkerType.DOG_PARK){
            openParkSheet(id);
        }else{
            int type = 0;
            if(markerType == MyClusteringItem.MarkerType.DOG_FRIENDLY)
                type = 2;
            if(markerType == MyClusteringItem.MarkerType.HOSPITAL)
                type = 3;
            if(markerType == MyClusteringItem.MarkerType.SHELTER)
                type = 4;
            if(markerType == MyClusteringItem.MarkerType.STORE)
                type = 5;
            if(markerType == MyClusteringItem.MarkerType.HOTEL)
                type = 6;
            if(markerType == MyClusteringItem.MarkerType.TRAINING)
                type = 7;
            if(markerType == MyClusteringItem.MarkerType.BREEDING)
                type = 8;
            openPlaceSheet(id, type);
        }
    }

    public void openParkSheet(int id){
        ApiClient.getInstance()
                .getApi()
                .getMarker(id, 1)
                .enqueue(new Callback<jsonResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<jsonResponse> call, @NonNull Response<jsonResponse> response) {
                        if (response.isSuccessful()) {

                            // fill in everything
                            Gson gson = new Gson();
                            ParkMarkerInfo markerInfo = gson.fromJson(response.body().getJson(), ParkMarkerInfo.class);

                            TextView coordinates = parksBottomSheetLayout.findViewById(R.id.coordinates);
                            TextView address = parksBottomSheetLayout.findViewById(R.id.address);
                            TextView area = parksBottomSheetLayout.findViewById(R.id.area);
                            TextView lightning = parksBottomSheetLayout.findViewById(R.id.lighting);
                            TextView fencing = parksBottomSheetLayout.findViewById(R.id.fencing);
                            TextView elements = parksBottomSheetLayout.findViewById(R.id.elements_text_view);

                            TextView monday = parksBottomSheetLayout.findViewById(R.id.monday);
                            TextView tuesday = parksBottomSheetLayout.findViewById(R.id.tuesday);
                            TextView wednesday = parksBottomSheetLayout.findViewById(R.id.wednesday);
                            TextView thursday = parksBottomSheetLayout.findViewById(R.id.thursday);
                            TextView friday = parksBottomSheetLayout.findViewById(R.id.friday);
                            TextView saturday = parksBottomSheetLayout.findViewById(R.id.saturday);
                            TextView sunday = parksBottomSheetLayout.findViewById(R.id.sunday);

                            coordinates.setText(markerInfo.getLatitude() + ", " + markerInfo.getLongitude());

                            List<Address> addressList = null;

                            try {
                                addressList = geocoder.getFromLocation(markerInfo.getLatitude(), markerInfo.getLongitude(), 1);
                                if(addressList.size() > 0) {
                                    Address adr = addressList.get(0);
                                    address.setText(adr.getAddressLine(0));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            area.setText(String.valueOf(markerInfo.getArea()));
                            lightning.setText(markerInfo.getLighting() == 1 ? "Yes" : "No");
                            fencing.setText(markerInfo.getFencing() == 1 ? "Yes" : "No");
                            elements.setText(markerInfo.getElements());

                            if(!markerInfo.getWorking_hours().isEmpty()) {
                                String[] wh = markerInfo.getWorking_hours().split(", ");
                                monday.setText(wh[0]);
                                tuesday.setText(wh[1]);
                                wednesday.setText(wh[2]);
                                thursday.setText(wh[3]);
                                friday.setText(wh[4]);
                                saturday.setText(wh[5]);
                                sunday.setText(wh[6]);
                            }

                            Glide.with(MainActivity.this)
                                    .load(markerInfo.getImage())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.default_place_image)
                                    .centerCrop()
                                    .into((ImageView)parksBottomSheetLayout.findViewById(R.id.image));

                            // bottom sheet
                            parksBottomSheetBehavior.setPeekHeight(placesBottomSheetLayout.findViewById(R.id.topLinearLayout).getHeight());
                            parksBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        } else {
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(MainActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<jsonResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MainActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    public void openPlaceSheet(int id, int type){
        ApiClient.getInstance()
                .getApi()
                .getMarker(id, type)
                .enqueue(new Callback<jsonResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<jsonResponse> call, @NonNull Response<jsonResponse> response) {
                        if (response.isSuccessful()) {

                            // fill in everything
                            Gson gson = new Gson();
                            PlaceMarkerInfo markerInfo = gson.fromJson(response.body().getJson(), PlaceMarkerInfo.class);

                            TextView name = placesBottomSheetLayout.findViewById(R.id.name);
                            TextView coordinates = placesBottomSheetLayout.findViewById(R.id.coordinates);
                            TextView address = placesBottomSheetLayout.findViewById(R.id.address);
                            TextView contacts = placesBottomSheetLayout.findViewById(R.id.contacts);
                            TextView website = placesBottomSheetLayout.findViewById(R.id.website);
                            TextView details = placesBottomSheetLayout.findViewById(R.id.details_text_view);

                            TextView monday = placesBottomSheetLayout.findViewById(R.id.monday);
                            TextView tuesday = placesBottomSheetLayout.findViewById(R.id.tuesday);
                            TextView wednesday = placesBottomSheetLayout.findViewById(R.id.wednesday);
                            TextView thursday = placesBottomSheetLayout.findViewById(R.id.thursday);
                            TextView friday = placesBottomSheetLayout.findViewById(R.id.friday);
                            TextView saturday = placesBottomSheetLayout.findViewById(R.id.saturday);
                            TextView sunday = placesBottomSheetLayout.findViewById(R.id.sunday);

                            name.setText(markerInfo.getName());
                            coordinates.setText(markerInfo.getLatitude() + ", " + markerInfo.getLongitude());

                            List<Address> addressList = null;

                            try {
                                addressList = geocoder.getFromLocation(markerInfo.getLatitude(), markerInfo.getLongitude(), 1);
                                if(addressList.size() > 0) {
                                    Address adr = addressList.get(0);
                                    address.setText(adr.getAddressLine(0));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            contacts.setText(markerInfo.getContacts());
                            website.setText(markerInfo.getWebsite());
                            details.setText(markerInfo.getDetails());

                            if(!markerInfo.getWorking_hours().isEmpty()) {
                                String[] wh = markerInfo.getWorking_hours().split(", ");
                                monday.setText(wh[0]);
                                tuesday.setText(wh[1]);
                                wednesday.setText(wh[2]);
                                thursday.setText(wh[3]);
                                friday.setText(wh[4]);
                                saturday.setText(wh[5]);
                                sunday.setText(wh[6]);
                            }

                            Glide.with(MainActivity.this)
                                    .load(markerInfo.getImage())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.default_place_image)
                                    .centerCrop()
                                    .into((ImageView)placesBottomSheetLayout.findViewById(R.id.image));

                            // bottom sheet
                            placesBottomSheetBehavior.setPeekHeight(placesBottomSheetLayout.findViewById(R.id.topLinearLayout).getHeight());
                            placesBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        } else {
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(MainActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<jsonResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MainActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    public void openDogSheet(int id){
        ApiClient.getInstance()
                .getApi()
                .getMarker(id, 0)
                .enqueue(new Callback<jsonResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<jsonResponse> call, @NonNull Response<jsonResponse> response) {
                        if (response.isSuccessful()) {

                            Gson gson = new Gson();
                            DogMarkerInfo dogMarkerInfo = gson.fromJson(response.body().getJson(), DogMarkerInfo.class);

                            TextView name = dogBottomSheetLayout.findViewById(R.id.name);
                            TextView owner = dogBottomSheetLayout.findViewById(R.id.owner);
                            TextView breed = dogBottomSheetLayout.findViewById(R.id.breed);
                            TextView birthday = dogBottomSheetLayout.findViewById(R.id.birthday);

                            Glide.with(MainActivity.this)
                                    .load(dogMarkerInfo.getImage())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .circleCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.dog_photo_default)
                                    .into((ImageView)dogBottomSheetLayout.findViewById(R.id.image));

                            Glide.with(MainActivity.this)
                                    .load(dogMarkerInfo.getSex() == 0 ? R.drawable.male_icon_orange : R.drawable.female_icon_orange)
                                    .into((ImageView)dogBottomSheetLayout.findViewById(R.id.sex));

                            name.setText(dogMarkerInfo.getName());
                            owner.setText(dogMarkerInfo.getUser_name());
                            breed.setText(dogMarkerInfo.getBreed());
                            birthday.setText(dogMarkerInfo.getBirthday());

                            owner.setOnClickListener(v -> {
                                user_id = dogMarkerInfo.getUser_id();
                                navController.navigate(R.id.action_map_to_profile);
                                dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            });

                            dogBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        } else {
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(MainActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(MainActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<jsonResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MainActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        user_id = -1;
        super.onBackPressed();
    }
}