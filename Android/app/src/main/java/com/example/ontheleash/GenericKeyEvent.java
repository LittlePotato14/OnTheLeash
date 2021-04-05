package com.example.ontheleash;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class GenericKeyEvent implements View.OnKeyListener{
    private final EditText currentView;
    private final EditText previousView;


    public GenericKeyEvent(EditText currentView, EditText previousView){
        this.currentView = currentView;
        this.previousView = previousView;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DEL
                && previousView != null
                && currentView.getText().toString().isEmpty()) {
            previousView.setText(null);
            previousView.requestFocus();
            return true;
        }
        return false;
    }
}
