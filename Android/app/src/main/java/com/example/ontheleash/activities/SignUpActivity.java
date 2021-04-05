package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.RegistrationLoginRequest;
import com.example.ontheleash.api.JwtResponse;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEdit, passwordEdit, confirmPasswordEdit;
    private TextInputLayout emailInput, passwordInput, confirmPasswordInput;
    private String email, password, confirmPassword;
    private ImageButton forward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_OnTheLeash);
        setContentView(R.layout.activity_sign_up);

        SharedPreferences mSettings;
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);

        emailEdit = findViewById(R.id.editEmail);
        passwordEdit = findViewById(R.id.editPassword);
        confirmPasswordEdit = findViewById(R.id.editConfirmPassword);
        emailInput = findViewById(R.id.emailInputLayout);
        passwordInput = findViewById(R.id.passwordInputLayout);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInputLayout);

        Button signUp = findViewById(R.id.signUp);
        TextView textLogin = findViewById(R.id.textLogin);
        forward = findViewById(R.id.forwardButton);
        ImageButton back = findViewById(R.id.backButton);

        // check if user is not on confirmation code stage yet
        if(!mSettings.getBoolean(Settings.APP_PREFERENCES_CODE_STAGE, false)) {
            forward.setEnabled(false);
            forward.setImageResource(R.drawable.forward_icon_white);
        }

        // open log in activity
        textLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        // open confirmation code activity
        forward.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, ConfirmationCodeActivity.class);
            startActivity(intent);
        });

        // get back to the main activity
        back.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // send registration request
        signUp.setOnClickListener(v -> apiSignUp());
    }

    private void apiSignUp() {
        // check format of user's credentials
        email = emailEdit.getText().toString();
        password = passwordEdit.getText().toString();
        confirmPassword = confirmPasswordEdit.getText().toString();

        if (!checkCredentials())
            return;

        // send login request
        ApiClient.getInstance()
                .getApi()
                .registration(new RegistrationLoginRequest(email, password))
                .enqueue(new Callback<JwtResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<JwtResponse> call, @NonNull Response<JwtResponse> response) {
                        if(response.isSuccessful()) {
                            String jwt = response.body().getJwt();
                            System.out.println(jwt);

                            // save jwt
                            SharedPreferences mSettings;
                            mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString(Settings.APP_PREFERENCES_TOKEN, jwt);
                            // past registration, waiting for code
                            editor.putBoolean(Settings.APP_PREFERENCES_CODE_STAGE, true);
                            editor.apply();

                            forward.setImageResource(R.drawable.forward_icon_green);

                            // open confirmation code activity
                            Intent intent = new Intent(SignUpActivity.this, ConfirmationCodeActivity.class);
                            startActivity(intent);
                        }
                        else{
                            switch (response.code()) {
                                case 422:
                                    Toast.makeText(SignUpActivity.this, "Email address already exists", Toast.LENGTH_SHORT).show();
                                    emailInput.setError("Email address already exists");
                                    break;
                                case 500:
                                    Toast.makeText(SignUpActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(SignUpActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }


                    @Override
                    public void onFailure(@NonNull Call<JwtResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(),
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    /**
     * Check if email and password corresponds format
     */
    private boolean checkCredentials(){
        emailInput.setError(null);
        passwordInput.setError(null);
        confirmPasswordInput.setError(null);

        if(!Pattern.matches(Settings.EMAIL_PATTERN, email)){
            emailInput.setError("Wrong email format");
            return false;
        }

        if(!Pattern.matches(Settings.PASSWORD_PATTERN, password)){
            passwordInput.setError("Wrong password format");
            return false;
        }

        if(!password.equals(confirmPassword)){
            passwordInput.setError("");
            confirmPasswordInput.setError("Password does not match");
            return false;
        }

        return true;
    }
}