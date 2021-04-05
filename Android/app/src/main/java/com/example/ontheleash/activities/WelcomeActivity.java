package com.example.ontheleash.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button explore = findViewById(R.id.explore);
        explore.setOnClickListener(v -> explore());
    }

    private void explore(){
        // setting user is not first time in app
        SharedPreferences mSettings;
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Settings.APP_PREFERENCES_FIRST_IN, false);
        editor.apply();

        // open main activity
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}