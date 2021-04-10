package com.example.ontheleash.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;

import org.jetbrains.annotations.NotNull;


public class MapFragment extends Fragment {
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

//        ImageButton topButton = view.findViewById(R.id.topButton);
//        topButton.setEnabled(true);
//        topButton.setVisibility(View.VISIBLE);
//        topButton.setImageResource(R.drawable.map_settings_icon_white);

        return view;
    }
}