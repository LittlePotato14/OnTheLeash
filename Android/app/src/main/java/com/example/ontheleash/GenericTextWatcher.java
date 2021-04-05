package com.example.ontheleash;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class GenericTextWatcher implements TextWatcher {

    private final EditText nextView;

    public GenericTextWatcher(EditText nextView){
        this.nextView = nextView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        if(nextView != null && text.length() == 1) {
            nextView.requestFocus();
        }
        else if(nextView == null){
            //ToDo hide the keyboard
        }
    }
}
