package com.example.ontheleash.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;

public class MainActivity extends AppCompatActivity {

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

        Button signUp = findViewById(R.id.signUp);
        Button login = findViewById(R.id.login);

        // open sign up activity
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // open login activity
        login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
        });
    }
}