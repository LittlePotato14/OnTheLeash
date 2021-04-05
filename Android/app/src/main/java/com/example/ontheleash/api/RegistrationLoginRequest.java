package com.example.ontheleash.api;

public class RegistrationLoginRequest {
    private String email;
    private String password;

    public RegistrationLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
