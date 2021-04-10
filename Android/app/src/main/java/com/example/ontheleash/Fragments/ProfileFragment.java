package com.example.ontheleash.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.activities.LogInActivity;
import com.example.ontheleash.activities.SignUpActivity;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        SharedPreferences mSettings;
        mSettings = this.getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

        if(mSettings.getBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false))
            view = inflater.inflate(R.layout.fragment_profile, container, false);
        else {
            view = inflater.inflate(R.layout.fragment_profile_unauthorised, container, false);

            Button signUp = view.findViewById(R.id.signUp);
            Button login = view.findViewById(R.id.login);

            // open sign up activity
            signUp.setOnClickListener(v -> {
                Intent intent = new Intent(this.getActivity(), SignUpActivity.class);
                startActivity(intent);
            });

            // open login activity
            login.setOnClickListener(v -> {
                Intent intent = new Intent(this.getActivity(), LogInActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}