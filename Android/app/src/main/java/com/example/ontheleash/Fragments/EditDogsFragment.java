package com.example.ontheleash.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ontheleash.R;
import com.example.ontheleash.adapters.EditDogAdapter;
import com.example.ontheleash.dataClasses.User;

import static android.app.Activity.RESULT_OK;

public class EditDogsFragment extends Fragment {

    private User user;
    private RecyclerView recycler;
    private EditDogAdapter adapter;
    private final int IMAGE_DIALOG_REQUEST_CODE = 201;
    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 202;

    public EditDogsFragment() {
        // Required empty public constructor
    }

    public EditDogsFragment(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_dogs, container, false);

        recycler = view.findViewById(R.id.recycler);
        adapter = new EditDogAdapter(user.getDogs(), getActivity(), this);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), RecyclerView.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.recycler_divider_gray));
        recycler.addItemDecoration(divider);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            adapter.items.get(requestCode).uri = uri;
            adapter.notifyDataSetChanged();
        }
    }
}