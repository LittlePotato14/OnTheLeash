package com.example.ontheleash;

public class Settings {
    // Shared prefs
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_TOKEN = "jwt";
    public static final String APP_PREFERENCES_LOGGED_IN = "loggedIn";
    public static final String APP_PREFERENCES_FIRST_IN = "firstInApp";
    // if user already pass registration and has to input confirmation code
    public static final String APP_PREFERENCES_CODE_STAGE = "code";
    public static final String APP_PREFERENCES_MAP_SETTINGS_JSON = "mapSettings";

    // Patterns
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+[.][a-zA-Z0-9-.]+$";
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
}
