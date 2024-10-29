package com.example.flowapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        // Assuming you have a method to get the current user ID or user PIN
        int userId = 1; // Replace with actual user ID retrieval logic

        // Fetch the user's balance from the database
        double balance = getUserBalance(userId);
        String formattedBalance = formatToRupiah(balance);
        tvBalance.setText(formattedBalance);
    }

    private double getUserBalance(int userId) {
        // Logic to retrieve user_money from the database
        double balance = 0.0;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_MONEY},
                DatabaseHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                balance = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_MONEY));
            }
            cursor.close();
        }
        return balance;
    }

    private String formatToRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }
}