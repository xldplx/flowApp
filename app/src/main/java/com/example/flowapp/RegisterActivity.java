package com.example.flowapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etPin;
    private Button btnRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etPin = findViewById(R.id.et_pin);
        btnRegister = findViewById(R.id.btn_register);
        databaseHelper = new DatabaseHelper(this);

        // Set up the number buttons
        setupKeypad();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = etPin.getText().toString().trim();
                if (!pin.isEmpty()) {
                    if (!databaseHelper.checkUser (pin)) {
                        databaseHelper.addUser (pin);
                        Toast.makeText(RegisterActivity.this, "User  registered successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity after registration
                    } else {
                        Toast.makeText(RegisterActivity.this, "User  already registered!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Please enter a PIN", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupKeypad() {
        // Set up each button to append its number to the EditText
        for (int i = 0; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn_" + i, "id", getPackageName());
            Button btnNumber = findViewById(resId);
            int finalI = i;
            btnNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appendToPin(String.valueOf(finalI));
                }
            });
        }

        // Set up backspace button
        ImageButton btnBackspace = findViewById(R.id.btn_backspace);
        btnBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPin = etPin.getText().toString();
                if (currentPin.length() > 0) {
                    etPin.setText(currentPin.substring(0, currentPin.length() - 1)); // Remove last character
                }
            }
        });
    }

    private void appendToPin(String number) {
        String currentPin = etPin.getText().toString();
        if (currentPin.length() < 6) { // Limit PIN length to 6
            etPin.setText(currentPin + number);
        }
    }
}