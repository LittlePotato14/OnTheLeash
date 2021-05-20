package com.example.ontheleash.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.ontheleash.MyClusteringItem;
import com.example.ontheleash.OwnIconRendered;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.activities.EditUserActivity;
import com.example.ontheleash.activities.MainActivity;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.LatitudeLongitude;
import com.example.ontheleash.dataClasses.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {

    GoogleMap map;

    private User user;
    private EditText userName;
    private EditText name;
    private EditText description;
    private ImageView avatar;
    private Uri uri;

    private TextInputEditText editCoordinates;

    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 101;
    private final int IMAGE_DIALOG_REQUEST_CODE = 102;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public EditProfileFragment(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editCoordinates = view.findViewById(R.id.editCoordinates);

        userName = view.findViewById(R.id.editUsername);
        userName.setText(user.getUserName());
        name = view.findViewById(R.id.editName);
        name.setText(user.getName());
        description = view.findViewById(R.id.editDescription);
        description.setText(user.getDescription());

        Dialog image_click_dialog = new Dialog(getContext());
        image_click_dialog.setContentView(R.layout.dialog_on_image_click);
        image_click_dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg));
        image_click_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView upload_photo = image_click_dialog.findViewById(R.id.upload);
        TextView delete_photo = image_click_dialog.findViewById(R.id.delete);

        upload_photo.setOnClickListener(v1 -> {
            image_click_dialog.dismiss();

            setupPermissions();
        });

        delete_photo.setOnClickListener(v1 -> {
            deletePhoto();
            image_click_dialog.dismiss();}
            );

        avatar = view.findViewById(R.id.imageAvatar);
        avatar.setOnClickListener(v -> image_click_dialog.show());

        Glide.with(getContext())
                .load(user.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.default_avatar_image)
                .into(avatar);

        view.findViewById(R.id.save).setOnClickListener(v -> {
            userName.setError(null);
            updateUserApi();
        });

        // initialize map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // async map
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                // remove all POIs
                map.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getContext(), R.raw.map_style_json));

                if(user.getIs_location_set() == 1){
                    MarkerOptions mo = new MarkerOptions();

                    Geocoder geocoder = new Geocoder(getActivity());
                    try {
                        List<Address> addressList = null;
                        addressList = geocoder.getFromLocation(user.getLatitude(), user.getLongitude(), 1);
                        if(addressList.size() > 0) {
                            Address adr = addressList.get(0);
                            mo.title(adr.getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mo.position(new LatLng(user.getLatitude(), user.getLongitude()));
                    map.clear();
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(user.getLatitude(), user.getLongitude()), 10f));
                    map.addMarker(mo);

                    editCoordinates.setText(user.getLatitude() + ", " + user.getLongitude());
                }

                map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        avatar.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                });

                map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        avatar.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                });

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        // set coordinates in textview
                        editCoordinates.setText(latLng.latitude + ", " + latLng.longitude);
                    }
                });
            }
        });

        editCoordinates.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkCoordinates(editCoordinates.getText().toString())){
                    LatLng latLng = new LatLng(Double.parseDouble(editCoordinates.getText().toString().split(", ")[0]),
                            Double.parseDouble(editCoordinates.getText().toString().split(", ")[1]));

                    MarkerOptions mo = new MarkerOptions();

                    Geocoder geocoder = new Geocoder(getActivity());
                    try {
                        List<Address> addressList = null;
                        addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if(addressList.size() > 0) {
                            Address adr = addressList.get(0);
                            mo.title(adr.getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mo.position(latLng);
                    map.clear();
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
                    map.addMarker(mo);
                }
            }
        });

        view.findViewById(R.id.saveMap).setOnClickListener(v -> {
            if(!checkCoordinates(editCoordinates.getText().toString())){
                Toast.makeText(getActivity(), "Wrong coordinates type", Toast.LENGTH_SHORT).show();
            }else
                updateLocationApi(Double.parseDouble(editCoordinates.getText().toString().split(", ")[0]),
                        Double.parseDouble(editCoordinates.getText().toString().split(", ")[1]));
        });

        return view;
    }

    private boolean checkCoordinates(String coordinates){
        String decimalPattern = "([0-9]*)\\.([0-9]*)";

        String[] coos = coordinates.split(", ");
        return coos.length == 2
                && Pattern.matches(decimalPattern, coos[0]) && Pattern.matches(decimalPattern, coos[1])
                && Double.parseDouble(coos[0]) <= 90 && Double.parseDouble(coos[0]) >= -90
                && Double.parseDouble(coos[1]) <= 180 && Double.parseDouble(coos[1]) >= -180;
    }

    private void deletePhoto(){
        Glide.with(getContext())
                .load(R.drawable.default_avatar_image)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(avatar);
        user.delete_photo = 1;
        uri = null;
    }

    private void updateLocationApi(double latitude, double longitude){
        SharedPreferences mSettings;
        Context context = getContext();
        mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        String jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");

        ApiClient.getInstance()
                .getApi()
                .updateLocation(jwt, new LatitudeLongitude(latitude, longitude))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    SharedPreferences mSettings;
                                    mSettings = getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = mSettings.edit();
                                    editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
                                    editor.apply();
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                    break;
                                case 500:
                                    Toast.makeText(context, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void updateUserApi(){
        SharedPreferences mSettings;
        Context context = getContext();
        mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        String jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");

        user.setUsername(userName.getText().toString());
        user.setName(name.getText().toString());
        user.setDescription(description.getText().toString());

        MultipartBody.Part part = null;

        if (uri != null){
            user.delete_photo = 0;
            File file = new File(getPathFromURI(uri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            part = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        }

        ApiClient.getInstance()
                .getApi()
                .updateUser(jwt, part, user)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {

                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    SharedPreferences mSettings;
                                    mSettings = getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = mSettings.edit();
                                    editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
                                    editor.apply();
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                    break;
                                case 422:
                                    Toast.makeText(context, "This username is taken", Toast.LENGTH_SHORT).show();
                                    userName.setError("This username is taken");
                                    break;
                                case 500:
                                    Toast.makeText(context, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    public String getPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContext().getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_DIALOG_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();

            Glide.with(getContext())
                    .load(uri)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.default_avatar_image)
                    .into(avatar);
        }
    }

    private void setupPermissions(){
        int permission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED)
            makeRequest();
        else
            openGallery();
    }

    private void makeRequest(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                EXTERNAL_STORAGE_REQUEST_CODE);
    }

    /* override it in parent activity
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        }
    }*/

    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/+");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image from gallery"), IMAGE_DIALOG_REQUEST_CODE);
    }
}