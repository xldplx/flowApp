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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay, container, false);

        etRecipient = view.findViewById(R.id.et_recipient);
        etAmount = view.findViewById(R.id.et_amount);
        btnPay = view.findViewById(R.id.btn_pay);
        btnScanQR = view.findViewById(R.id.btn_scan_qr);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = etRecipient.getText().toString();
                String amount = etAmount.getText().toString();
                if (!recipient.isEmpty() && !amount.isEmpty()) {
                    // Implement payment logic here
                    Toast.makeText(getContext(), "Payment of " + amount + " to " + recipient + " initiated", Toast.LENGTH_SHORT).show();
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