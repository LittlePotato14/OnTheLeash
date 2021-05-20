package com.example.ontheleash.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.ontheleash.R;
import com.example.ontheleash.activities.EditUserActivity;
import com.example.ontheleash.dataClasses.User;

public class AddDogButtonFragment extends Fragment {

    User user;
    ProfileFragment profileFragment;

    public AddDogButtonFragment() {
        // Required empty public constructor
    }

    public AddDogButtonFragment(User user, ProfileFragment profileFragment) {
        this.user = user;
        this.profileFragment = profileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_dog_button, container, false);

        ImageButton addDog = view.findViewById(R.id.add_dog);
        addDog.setEnabled(true);
        addDog.setVisibility(View.VISIBLE);
        addDog.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditUserActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("user", user);
            b.putInt("fragment", 1);
            intent.putExtras(b);
            startActivityForResult(intent, 1);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        profileFragment.refresh();
    }
}