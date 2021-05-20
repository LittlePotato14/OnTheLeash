package com.example.ontheleash.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.activities.EditUserActivity;
import com.example.ontheleash.activities.LogInActivity;
import com.example.ontheleash.activities.MainActivity;
import com.example.ontheleash.activities.SignUpActivity;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.dataClasses.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    View view;
    DogsViewPagerFragment pagerFragment;
    AddDogButtonFragment buttonFragment;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences mSettings;
        mSettings = this.getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

        if(mSettings.getBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false)) {
            view = inflater.inflate(R.layout.fragment_profile, container, false);

            view.findViewById(R.id.name).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.username).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.description).setVisibility(View.INVISIBLE);

            view.findViewById(R.id.editButton).setVisibility(View.VISIBLE);
            view.findViewById(R.id.forwardButton).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.forwardButton).setEnabled(false);
            view.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.backButton).setEnabled(false);
            view.findViewById(R.id.dots).setVisibility(View.INVISIBLE);

            if(((MainActivity)getActivity()).user_id == -1)
                getCurrentUserApi(mSettings.getString(Settings.APP_PREFERENCES_TOKEN, ""), this);
            else
                getUserApi(mSettings.getString(Settings.APP_PREFERENCES_TOKEN, ""), ((MainActivity)getActivity()).user_id, this);
        }
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

    private void getUserApi(String jwt, int id, ProfileFragment frag){
        ApiClient.getInstance()
                .getApi()
                .getUserInfo(jwt, id)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        if(response.isSuccessful()) {
                            User user = response.body();

                            pagerFragment = new DogsViewPagerFragment(user);
                            //buttonFragment = new AddDogButtonFragment(user, frag);

                            TextView name = view.findViewById(R.id.name);
                            TextView username = view.findViewById(R.id.username);
                            TextView description = view.findViewById(R.id.description);

                            name.setText(user.getName());
                            username.setText("@" + user.getUserName());
                            description.setText(user.getDescription());

                            name.setVisibility(View.VISIBLE);
                            username.setVisibility(View.VISIBLE);
                            description.setVisibility(View.VISIBLE);

                            Glide.with(getContext())
                                    .load(user.getAvatar())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .circleCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.default_avatar_image)
                                    .into((ImageView)view.findViewById(R.id.avatar));

                            view.findViewById(R.id.editButton).setVisibility(View.INVISIBLE);

                            // open dog viewpager fragment
                            if(user.getDogs().size() != 0) {
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.profileFrame, pagerFragment)
                                        .commit();
                            }

                            if(user.getDogs().size() <= 1){
                                view.findViewById(R.id.forwardButton).setVisibility(View.INVISIBLE);
                                view.findViewById(R.id.forwardButton).setEnabled(false);
                                view.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
                                view.findViewById(R.id.backButton).setEnabled(false);
                                view.findViewById(R.id.dots).setVisibility(View.INVISIBLE);
                            }
                            else{
                                view.findViewById(R.id.forwardButton).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.forwardButton).setEnabled(true);
                                view.findViewById(R.id.backButton).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.backButton).setEnabled(true);
                                view.findViewById(R.id.dots).setVisibility(View.VISIBLE);

                                view.findViewById(R.id.forwardButton).setOnClickListener(v -> pagerFragment.next());

                                view.findViewById(R.id.backButton).setOnClickListener(v -> pagerFragment.previous());
                            }
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
                                    Toast.makeText(getContext(), "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void getCurrentUserApi(String jwt, ProfileFragment frag){
        ApiClient.getInstance()
                .getApi()
                .getCurrentUserInfo(jwt)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        if(response.isSuccessful()) {
                            User user = response.body();

                            pagerFragment = new DogsViewPagerFragment(user);
                            buttonFragment = new AddDogButtonFragment(user, frag);

                            TextView name = view.findViewById(R.id.name);
                            TextView username = view.findViewById(R.id.username);
                            TextView description = view.findViewById(R.id.description);

                            name.setText(user.getName());
                            username.setText("@" + user.getUserName());
                            description.setText(user.getDescription());

                            name.setVisibility(View.VISIBLE);
                            username.setVisibility(View.VISIBLE);
                            description.setVisibility(View.VISIBLE);

                            Glide.with(getContext())
                                    .load(user.getAvatar())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .circleCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.default_avatar_image)
                                    .into((ImageView)view.findViewById(R.id.avatar));

                            view.findViewById(R.id.editButton).setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), EditUserActivity.class);
                                Bundle b = new Bundle();
                                b.putParcelable("user", user);
                                b.putInt("fragment", 0);
                                intent.putExtras(b);
                                startActivityForResult(intent, 1);
                            });


                            // open button fragment
                            if(user.getDogs().size() == 0) {
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.profileFrame, buttonFragment)
                                        .commit();
                            }
                            // open dog viewpager fragment
                            else{
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.profileFrame, pagerFragment)
                                        .commit();
                            }

                            if(user.getDogs().size() <= 1){
                                view.findViewById(R.id.forwardButton).setVisibility(View.INVISIBLE);
                                view.findViewById(R.id.forwardButton).setEnabled(false);
                                view.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
                                view.findViewById(R.id.backButton).setEnabled(false);
                                view.findViewById(R.id.dots).setVisibility(View.INVISIBLE);
                            }
                            else{
                                view.findViewById(R.id.forwardButton).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.forwardButton).setEnabled(true);
                                view.findViewById(R.id.backButton).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.backButton).setEnabled(true);
                                view.findViewById(R.id.dots).setVisibility(View.VISIBLE);

                                view.findViewById(R.id.forwardButton).setOnClickListener(v -> pagerFragment.next());

                                view.findViewById(R.id.backButton).setOnClickListener(v -> pagerFragment.previous());
                            }
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
                                    Toast.makeText(getContext(), "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refresh();
    }

    public void refresh() {
        SharedPreferences mSettings;
        mSettings = this.getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        getCurrentUserApi(mSettings.getString(Settings.APP_PREFERENCES_TOKEN, ""), this);
    }
}