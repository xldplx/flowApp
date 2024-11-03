package com.example.flowapp;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.AuthenticationCallback;
import androidx.biometric.BiometricPrompt.PromptInfo;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;
import android.content.Intent;

public class PinLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private BiometricPrompt biometricPrompt;
    private EditText etPin; // Reference to the EditText for PIN input
    private StringBuilder currentPin;
    private static final int PIN_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);

        etPin = findViewById(R.id.et_pin); // Initialize the EditText
        currentPin = new StringBuilder();

        setupKeypadButtons();
        setupActionButtons();
    }

    private void setupKeypadButtons() {
        int[] buttonIds = {
                R.id.btn_1, R.id.btn_2, R.id.btn_3,
                R.id.btn_4, R.id.btn_5, R.id.btn_6,
                R.id.btn_7, R.id.btn_8, R.id.btn_9,
                R.id.btn_0
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void setupActionButtons() {
        findViewById(R.id.btn_fingerprint).setOnClickListener(this);
        findViewById(R.id.btn_backspace).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(v -> {
            Intent intent = new Intent(PinLoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_fingerprint) {
            handleFingerprintClick();
        } else if (v.getId() == R.id.btn_backspace) {
            handleBackspaceClick();
        } else {
            handleNumberClick((Button) v);
        }
    }

    private void handleNumberClick(Button clickedButton) {
        if (currentPin.length() < PIN_LENGTH) {
            currentPin.append(clickedButton.getText());
            etPin.setText(currentPin.toString()); // Update EditText with current PIN
            if (currentPin.length() == PIN_LENGTH) {
                validatePin();
            }
        }
    }

    private void handleBackspaceClick() {
        if (currentPin.length() > 0) {
            currentPin.setLength(currentPin.length() - 1);
            etPin.setText(currentPin.toString()); // Update EditText after backspace
        }
    }

    private void validatePin() {
        String pin = currentPin.toString();
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        if (databaseHelper.checkUser (pin)) {
            // PIN is valid, retrieve user ID
            int userId = databaseHelper.getUserIdByPin(pin);
            UserSession.getInstance(this).setUserId(userId); // Store user ID in UserSession

            // Proceed to MainActivity
            Intent intent = new Intent(PinLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        } else {
            // PIN is invalid
            Toast.makeText(this, "Invalid PIN. Please try again.", Toast.LENGTH_SHORT).show();
            currentPin.setLength(0); // Clear the current PIN
            etPin.setText(""); // Clear the EditText
        }
    }

    private void handleFingerprintClick() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Authentication succeeded, retrieve user ID from UserSession
                int userId = UserSession.getInstance(PinLoginActivity.this).getUserId();
                if (userId != -1) { // Check if user ID is valid
                    // Proceed to MainActivity
                    Intent intent = new Intent(PinLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close this activity
                } else {
                    Toast.makeText(PinLoginActivity.this, "No user ID found. Please log in with PIN.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Authentication failed, show an error message
                Toast.makeText(PinLoginActivity.this, "Fingerprint authentication failed: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Authentication failed, show an error message
                Toast.makeText(PinLoginActivity.this, "Fingerprint authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setDescription("Authenticate using your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}