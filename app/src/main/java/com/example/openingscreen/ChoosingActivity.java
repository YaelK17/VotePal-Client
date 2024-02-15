package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.RadioButton;

public class ChoosingActivity extends AppCompatActivity {
    RadioButton one;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);
        one = findViewById(R.id.radio_option1);
    }

}