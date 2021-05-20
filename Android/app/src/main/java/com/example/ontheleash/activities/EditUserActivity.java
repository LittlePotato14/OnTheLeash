package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.ontheleash.Fragments.EditDogsFragment;
import com.example.ontheleash.Fragments.EditProfileFragment;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.dataClasses.User;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserActivity extends AppCompatActivity {

    User user;
    EditProfileFragment profileFragment;
    EditDogsFragment dogsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_OnTheLeash);
        setContentView(R.layout.activity_edit_user);

        // getting user from profile
        Bundle b = getIntent().getExtras();
        user = b.getParcelable("user");

        profileFragment = new EditProfileFragment(user);
        dogsFragment = new EditDogsFragment(user);

        MaterialButton profile = findViewById(R.id.profile);
        MaterialButton dogs = findViewById(R.id.dogs);

        // what fragment to open
        Fragment toStart = b.getInt("fragment") == 0 ? profileFragment : dogsFragment;

        // change buttons' styles
        if(b.getInt("fragment") == 1){
            profile.setStrokeWidth(0);
            profile.setEnabled(true);
            dogs.setStrokeWidth(3);
            dogs.setEnabled(false);
        }

        // open fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, toStart)
                .commit();

        // open profile fragment
        profile.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, profileFragment)
                    .commit();
            profile.setStrokeWidth(3);
            profile.setEnabled(false);
            dogs.setStrokeWidth(0);
            dogs.setEnabled(true);
        });

        // open dogs fragment
        dogs.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, dogsFragment)
                    .commit();
            profile.setStrokeWidth(0);
            profile.setEnabled(true);
            dogs.setStrokeWidth(3);
            dogs.setEnabled(false);
        });

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }

    /**
     * opens gallery in fragment, when user agrees the permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == EditProfileFragment.EXTERNAL_STORAGE_REQUEST_CODE){
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Need external storage permission", Toast.LENGTH_SHORT);
            else{
                profileFragment.openGallery();
            }
        }
        else if(requestCode == EditDogsFragment.EXTERNAL_STORAGE_REQUEST_CODE){
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Need external storage permission", Toast.LENGTH_SHORT);
        }
    }
}