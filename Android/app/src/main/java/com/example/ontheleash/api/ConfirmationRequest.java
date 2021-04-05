package com.example.ontheleash.api;

public class ConfirmationRequest {
    private String confirmation_code;

    public ConfirmationRequest(String code) {
        this.confirmation_code = code;
    }

    public String getCode() {
        return confirmation_code;
    }
}
