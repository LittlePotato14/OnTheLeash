package com.example.ontheleash.api;

public class DogResponse {
    int dog_id;

    public DogResponse(int id) {
        this.dog_id = id;
    }

    public int getDog_id() {
        return dog_id;
    }
}
