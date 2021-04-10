package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ImageButton topButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove splash screen
        setTheme(R.style.Theme_OnTheLeash);

        SharedPreferences mSettings;
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

        // first time in app => open welcome activity
        if(mSettings.getBoolean(Settings.APP_PREFERENCES_FIRST_IN, true)) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);

        topButton = findViewById(R.id.topButton);
        topButton.setEnabled(false);
        topButton.setVisibility(View.INVISIBLE);

        // connect menu icon to a drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // in other way icon are black
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setItemIconTintList(null);

        // set controller
        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);
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
                            if(response.isSuccessful()) {
                                // refresh profile fragment
                                navController.navigate(R.id.action_profile_to_profile);
                            }
                            else{
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

                switch (destination.getId()){
                    case R.id.map:
                        topButton.setEnabled(true);
                        topButton.setVisibility(View.VISIBLE);
                        topButton.setImageResource(R.drawable.map_settings_icon_white);
                        topButtonMap();
                        break;
                    case R.id.messages:
                    case R.id.support:
                    case R.id.settings:
                        topButton.setEnabled(false);
                        topButton.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.profile:
                        if(mSettings.getBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false)){
                            topButton.setEnabled(true);
                            topButton.setVisibility(View.VISIBLE);
                            topButton.setImageResource(R.drawable.logout_icon_white);
                            topButton.setOnClickListener(v -> logoutDialog.show());
                        }
                        else{
                            topButton.setEnabled(false);
                            topButton.setVisibility(View.INVISIBLE);
                        }

                        break;
                }
            }
        });
    }

    //ToDo
    private void topButtonMap(){

    }
}