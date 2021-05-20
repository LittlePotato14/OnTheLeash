package com.example.ontheleash.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.activities.EditUserActivity;
import com.example.ontheleash.activities.MainActivity;
import com.example.ontheleash.activities.WelcomeActivity;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.Email;
import com.example.ontheleash.api.Password;
import com.example.ontheleash.dataClasses.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    TextInputLayout emailLayout;
    TextInputLayout passwordLayout;
    TextInputLayout confirmPasswordLayout;

    TextInputEditText email;
    TextInputEditText password;
    TextInputEditText confirmPassword;

    SharedPreferences mSettings;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        emailLayout = view.findViewById(R.id.emailInputLayout);
        passwordLayout = view.findViewById(R.id.passwordInputLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordInputLayout);

        email = view.findViewById(R.id.editEmail);
        password = view.findViewById(R.id.editPassword);
        confirmPassword = view.findViewById(R.id.editConfirmPassword);

        mSettings = this.getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        String jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");

        view.findViewById(R.id.saveEmail).setOnClickListener(v -> {
            emailLayout.setError(null);
            if(email.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Fill in email address", Toast.LENGTH_SHORT).show();
                emailLayout.setError("Fill in email address");
                return;
            }
            updateEmail(jwt);
        });

        view.findViewById(R.id.savePassword).setOnClickListener(v -> {
            passwordLayout.setError(null);
            confirmPasswordLayout.setError(null);
            if(password.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Fill in password", Toast.LENGTH_SHORT).show();
                passwordLayout.setError("Fill in password");
                return;
            }
            if(!Pattern.matches(Settings.PASSWORD_PATTERN, password.getText().toString())){
                Toast.makeText(getContext(), "Weak password", Toast.LENGTH_SHORT).show();
                passwordLayout.setError("Weak password");
                return;
            }
            if(!password.getText().toString().equals(confirmPassword.getText().toString())){
                Toast.makeText(getContext(), "Password does not match", Toast.LENGTH_SHORT).show();
                passwordLayout.setError("Password does not match");
                confirmPasswordLayout.setError("Password does not match");
                return;
            }
            updatePassword(jwt);
        });

        view.findViewById(R.id.closeOthers).setOnClickListener(v -> deleteOtherSessions(jwt));


        Dialog deleteDialog = new Dialog(getContext());
        deleteDialog.setContentView(R.layout.dialog_delete_account);
        deleteDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg));
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView no = deleteDialog.findViewById(R.id.no);
        TextView yes = deleteDialog.findViewById(R.id.yes);

        no.setOnClickListener(v1 -> deleteDialog.dismiss());

        yes.setOnClickListener(v1 -> {
            // close dialog
            deleteDialog.dismiss();
            deleteUser(jwt);
        });

        view.findViewById(R.id.deleteAccount).setOnClickListener(v -> deleteDialog.show());

        return view;
    }

    private void updateEmail(String jwt){
        ApiClient.getInstance()
                .getApi()
                .updateEmail(jwt, new Email(email.getText().toString()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(getContext(), "Email address was changed", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrongJwt();
                                    break;
                                case 422:
                                    Toast.makeText(getContext(), "This email address already exists", Toast.LENGTH_SHORT).show();
                                    emailLayout.setError("This email address already exists");
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
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void updatePassword(String jwt){
        ApiClient.getInstance()
                .getApi()
                .updatePassword(jwt, new Password(password.getText().toString()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(getContext(), "Password was changed", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrongJwt();
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
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void deleteUser(String jwt){
        ApiClient.getInstance()
                .getApi()
                .deleteUser(jwt)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            // open main activity

                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
                            editor.apply();

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrongJwt();
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
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void deleteOtherSessions(String jwt){
        ApiClient.getInstance()
                .getApi()
                .deleteOtherSessions(jwt)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(getContext(), "All other sessions were deleted", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrongJwt();
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
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void wrongJwt(){
        SharedPreferences mSettings;
        mSettings = getActivity().getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
        editor.apply();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}