package com.example.flowapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.NumberFormat;
import java.util.Locale;

public class WalletFragment extends Fragment {

    private TextView tvBalance;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        tvBalance = view.findViewById(R.id.tv_balance);
        databaseHelper = new DatabaseHelper(getActivity());

        // Fetch and display the user's balance
        displayUserBalance();

        // Set up button listener
        MaterialButton btnAddMoney = view.findViewById(R.id.btn_add_money);
        btnAddMoney.setOnClickListener(v -> {
            // Handle add money button click
            // You can start a new fragment or activity to handle top-up
        });

        return view;
    }

    private void displayUserBalance() {
        int userId = UserSession.getInstance(getActivity()).getUserId(); // Retrieve user ID from UserSession

        // Fetch the user's balance from the database
        double balance = getUserBalance(userId);
        String formattedBalance = formatToRupiah(balance);
        tvBalance.setText(formattedBalance);
    }

    private double getUserBalance(int userId) {
        return databaseHelper.getUserBalance(userId); // Use the existing method in DatabaseHelper
    }

    private String formatToRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }
}