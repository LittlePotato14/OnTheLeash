package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.ConfirmationRequest;
import com.example.ontheleash.api.ForgotPasswordRequest;
import com.example.ontheleash.api.JwtResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_OnTheLeash);
        setContentView(R.layout.activity_forgot_password);

        Button send = findViewById(R.id.send);
        ImageButton back = findViewById(R.id.backButton);
        email = findViewById(R.id.email);

        back.setOnClickListener(v -> onBackPressed());

        send.setOnClickListener(v -> apiSend());
    }

    private void apiSend(){
        if(email.getText().toString().isEmpty()){
            Toast.makeText(ForgotPasswordActivity.this, "Please, fill in email address", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getInstance()
                .getApi()
                .forgotPassword(new ForgotPasswordRequest(email.getText().toString()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            onBackPressed();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    Toast.makeText(ForgotPasswordActivity.this, "Email does not exists", Toast.LENGTH_SHORT).show();
                                    break;
                                case 500:
                                    Toast.makeText(ForgotPasswordActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(ForgotPasswordActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }
}