package com.example.ontheleash.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ontheleash.R;
import com.example.ontheleash.adapters.DogPagerAdapter;
import com.example.ontheleash.dataClasses.User;

public class DogsViewPagerFragment extends Fragment {

    User user;
    ViewPager2 viewPager;

    public DogsViewPagerFragment() {
        // Required empty public constructor
    }

    public DogsViewPagerFragment(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dogs_view_pager, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        DogPagerAdapter adapter = new DogPagerAdapter(user.getDogs());
        viewPager.setAdapter(adapter);

        return view;
    }

    public void next(){
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    public void previous(){
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }
}