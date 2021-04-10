package com.example.ontheleash.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.RegistrationLoginRequest;
import com.example.ontheleash.api.JwtResponse;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private EditText emailEdit, passwordEdit;
    private TextInputLayout emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_OnTheLeash);
        setContentView(R.layout.activity_log_in);

        emailEdit = findViewById(R.id.editEmail);
        passwordEdit = findViewById(R.id.editPassword);
        emailInput = findViewById(R.id.emailInputLayout);
        passwordInput = findViewById(R.id.passwordInputLayout);

        Button login = findViewById(R.id.login);
        TextView textSignUp = findViewById(R.id.textSignUp);
        TextView textForgotPassword = findViewById(R.id.textForgotPassword);
        ImageButton back = findViewById(R.id.backButton);

        // open activity forgot password
        textForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // send login request
        login.setOnClickListener(v -> apiLogIn());

        // open sign up activity
        textSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // get back to the main activity
        back.setOnClickListener(v -> onBackPressed());
    }

    private void openMainActivity(){
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void apiLogIn(){
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        emailInput.setError(null);
        passwordInput.setError(null);

        if(email.isEmpty()){
            emailInput.setError("Input email address");
            return;
        }

        if(password.isEmpty()){
            passwordInput.setError("Input password");
            return;
        }

        // send login request
        ApiClient.getInstance()
                .getApi()
                .login(new RegistrationLoginRequest(email, password))
                .enqueue(new Callback<JwtResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<JwtResponse> call, @NonNull Response<JwtResponse> response) {
                        if(response.isSuccessful()) {
                            String jwt = response.body().getJwt();

                            // save jwt
                            SharedPreferences mSettings;
                            mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString(Settings.APP_PREFERENCES_TOKEN, jwt);
                            editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, true);
                            editor.putBoolean(Settings.APP_PREFERENCES_CODE_STAGE, false);
                            editor.apply();

                            openMainActivity();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    Toast.makeText(LogInActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                                    emailInput.setError("Wrong email or password");
                                    passwordInput.setError("Wrong email or password");
                                    break;
                                case 500:
                                    Toast.makeText(LogInActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(LogInActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JwtResponse> call, @NonNull Throwable t) {
                        Toast.makeText(LogInActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }
}