package com.example.ontheleash;

import android.view.View;
import android.widget.EditText;

public class GenericOnFocus implements View.OnFocusChangeListener {
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ((EditText)v).setText(null);
        }
    }
}
