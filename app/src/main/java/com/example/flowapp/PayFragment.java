package com.example.flowapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PayFragment extends Fragment {

    private EditText etRecipient;
    private EditText etAmount;
    private Button btnPay;
    private Button btnScanQR;

    private static final int QR_CODE_REQUEST = 100; // Request code for QR scanner activity

    private DatabaseHelper databaseHelper;
    private int userId; // Assume you have a way to get the current user's ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay, container, false);

        etRecipient = view.findViewById(R.id.et_recipient);
        etAmount = view.findViewById(R.id.et_amount);
        btnPay = view.findViewById(R.id.btn_pay);
        btnScanQR = view.findViewById(R.id.btn_scan_qr);

        databaseHelper = new DatabaseHelper(getContext());
        userId = UserSession.getInstance(getActivity()).getUserId(); // Get the current user's ID from session// Get the current user's ID from session

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = etRecipient.getText().toString();
                String amountStr = etAmount.getText().toString();

                if (!recipient.isEmpty() && !amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);
                    double currentBalance = databaseHelper.getUserBalance(userId);

                    if (currentBalance >= amount) {
                        // Proceed with payment
                        processPayment(recipient, amount);
                    } else {
                        Toast.makeText(getContext(), "Insufficient balance", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start QRScannerActivity
                Intent intent = new Intent(getActivity(), QRScannerActivity.class);
                startActivityForResult(intent, QR_CODE_REQUEST); // Start the activity for result
            }
        });

        return view;
    }

    private void processPayment(String recipient, double amount) {
        try {
            // Subtract the amount from the user's balance
            double newBalance = databaseHelper.getUserBalance(userId) - amount;
            databaseHelper.updateUserBalance(userId, newBalance); // Update the balance in the database

            // Create a new transaction record
            String date = "2023-05-01"; // Replace with the current date in your desired format
            String title = "Payment to " + recipient;
            String transactionType = "payment";

            // Add the transaction to the database
            databaseHelper.addTransaction(userId, amount, date, title, transactionType);

            Toast.makeText(getContext(), "Payment of " + amount + " to " + recipient + " successful", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception to Logcat
            Toast.makeText(getContext(), "Error processing payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Optionally, clear the input fields
        etRecipient.setText("");
        etAmount.setText("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_CODE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            String scannedData = data.getStringExtra("scannedData"); // Assuming you pass the scanned data back
            if (scannedData != null) {
                // Handle the scanned QR code result here
                Toast.makeText(getContext(), "Scanned: " + scannedData, Toast.LENGTH_LONG).show();
                // You can parse the QR code content and fill in the recipient and amount fields
                etRecipient.setText(scannedData); // Example of setting the recipient
                // Optionally, you can set a default amount or ask the user for it
            }
        }
    }
}