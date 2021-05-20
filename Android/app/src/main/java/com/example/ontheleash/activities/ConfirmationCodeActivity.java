package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ontheleash.GenericKeyEvent;
import com.example.ontheleash.GenericOnFocus;
import com.example.ontheleash.GenericTextWatcher;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.ConfirmationRequest;
import com.example.ontheleash.api.JwtResponse;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationCodeActivity extends AppCompatActivity {

    private TextView resendCode, nextResend;

    private EditText code1;
    private EditText code2;
    private EditText code3;
    private EditText code4;

    private Timer resendTimer;

    private String jwt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_OnTheLeash);
        setContentView(R.layout.activity_confirmation_code);

        // getting jwt token
        SharedPreferences mSettings;
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");

        resendCode = findViewById(R.id.resendCode);
        nextResend = findViewById(R.id.nextResend);
        Button confirm = findViewById(R.id.confirm);
        ImageButton back = findViewById(R.id.backButton);
        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);

        // timer for code resending
        resendTimer = new Timer();
        setTimer();

        // check confirmation code
        confirm.setOnClickListener(v -> apiConfirm());
        // resend code
        resendCode.setOnClickListener(v -> apiResend());
        // back to registration
        back.setOnClickListener(v -> onBackPressed());

        // For switching to the next
        code1.addTextChangedListener(new GenericTextWatcher(code2));
        code2.addTextChangedListener(new GenericTextWatcher(code3));
        code3.addTextChangedListener(new GenericTextWatcher(code4));
        code4.addTextChangedListener(new GenericTextWatcher(null));

        // For delete and switching to the previous
        code1.setOnKeyListener(new GenericKeyEvent(code1, null));
        code2.setOnKeyListener(new GenericKeyEvent(code2, code1));
        code3.setOnKeyListener(new GenericKeyEvent(code3, code2));
        code4.setOnKeyListener(new GenericKeyEvent(code4, code3));

        // For cleaning up when focusing on
        code1.setOnFocusChangeListener(new GenericOnFocus());
        code2.setOnFocusChangeListener(new GenericOnFocus());
        code3.setOnFocusChangeListener(new GenericOnFocus());
        code4.setOnFocusChangeListener(new GenericOnFocus());
    }

    /**
     * Sets timer on 10 seconds, after 10 sec makes resend code button visible and enabled
     */
    private void setTimer(){
        resendCode.setEnabled(false);
        resendCode.setTextColor(ContextCompat.getColor(this, R.color.light_gray));
        nextResend.setVisibility(View.VISIBLE);

        resendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        resendCode.setEnabled(true);
                        resendCode.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.secondary_orange));
                        nextResend.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, 10000);
    }

    /**
     * Send confirmation request
     */
    private void apiConfirm(){
        String a1 = code1.getText().toString(),
                a2 = code2.getText().toString(),
                a3 = code3.getText().toString(),
                a4 = code4.getText().toString();

        // check if code was typed in
        if(a1.isEmpty() || a2.isEmpty() || a3.isEmpty() || a4.isEmpty()){
            Toast.makeText(ConfirmationCodeActivity.this, "Please, input code", Toast.LENGTH_SHORT).show();
            return;
        }

        // sending request
        ApiClient.getInstance()
                .getApi()
                .confirm(jwt, new ConfirmationRequest(a1 + a2 + a3 + a4))
                .enqueue(new Callback<JwtResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<JwtResponse> call, @NonNull Response<JwtResponse> response) {
                        if(response.isSuccessful()) {
                            // getting active jwt
                            String jwt = response.body().getJwt();
                            System.out.println(jwt);

                            // save jwt and logged in status
                            SharedPreferences mSettings;
                            mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString(Settings.APP_PREFERENCES_TOKEN, jwt);
                            editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, true);
                            editor.putBoolean(Settings.APP_PREFERENCES_CODE_STAGE, false);
                            editor.apply();

                            // open main activity
                            Intent intent = new Intent(ConfirmationCodeActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    Toast.makeText(ConfirmationCodeActivity.this, "Incorrect confirmation code", Toast.LENGTH_SHORT).show();
                                    break;
                                case 410:
                                    code1.setEnabled(false);
                                    code2.setEnabled(false);
                                    code3.setEnabled(false);
                                    code4.setEnabled(false);
                                    Toast.makeText(ConfirmationCodeActivity.this, "You are out of attempts. Click the button 'Resend code' and try again", Toast.LENGTH_LONG).show();
                                    break;
                                case 500:
                                    Toast.makeText(ConfirmationCodeActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(ConfirmationCodeActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JwtResponse> call, @NonNull Throwable t) {
                        Toast.makeText(ConfirmationCodeActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    /**
     * Send request to resend confirmation code
     */
    private void apiResend(){
        setTimer();

        // send request
        ApiClient.getInstance()
                .getApi()
                .newCode(jwt)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(ConfirmationCodeActivity.this, "A new code was sent", Toast.LENGTH_SHORT).show();

                            // we made them disabled when out of attempt
                            code1.setEnabled(true);
                            code2.setEnabled(true);
                            code3.setEnabled(true);
                            code4.setEnabled(true);
                        }
                        else{
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(ConfirmationCodeActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(ConfirmationCodeActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(ConfirmationCodeActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }
}