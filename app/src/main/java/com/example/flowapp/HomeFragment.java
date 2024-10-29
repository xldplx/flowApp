package com.example.flowapp;

import android.database.Cursor;
import android.os.Bundle;
import java.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import Button
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvBalance;
    private DatabaseHelper databaseHelper; // Assuming you have a DatabaseHelper class

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBalance = view.findViewById(R.id.balance); // Reference to the balance TextView
        databaseHelper = new DatabaseHelper(getActivity()); // Initialize your DatabaseHelper

        // Fetch and display the user's balance
        displayUserBalance();

        // Set up the Top up button
        Button btnTopUp = view.findViewById(R.id.btn_top_up);
        btnTopUp.setOnClickListener(v -> navigateToTopupFragment());

        RecyclerView quickActionsRecycler = view.findViewById(R.id.quick_actions_recycler);
        quickActionsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 4));

        List<QuickAction> quickActions = new ArrayList<>();
        quickActions.add(new QuickAction(R.drawable.pay, "Pay"));
        quickActions.add(new QuickAction(R.drawable.invest, "Invest"));
        quickActions.add(new QuickAction(R.drawable.rewards, "Rewards"));
        quickActions.add(new QuickAction(R.drawable.help, "Help"));
        quickActions.add(new QuickAction(R.drawable.scan, "Scan"));
        quickActions.add(new QuickAction(R.drawable.pay_bills, "Pay Bills"));
        quickActions.add(new QuickAction(R.drawable.wallet, "Wallet"));
        quickActions.add(new QuickAction(R.drawable.transactions, "History"));

        QuickActionAdapter adapter = new QuickActionAdapter(quickActions, getContext());
        quickActionsRecycler.setAdapter(adapter);

        return view;
    }

    private void navigateToTopupFragment() {
        TopupFragment topupFragment = new TopupFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, topupFragment) // Ensure this ID matches the container in your activity layout
                .addToBackStack(null) // Optional: adds the transaction to the back stack
                .commit();
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
        double balance = 0.0;
        Cursor cursor = databaseHelper.getUserBalance(userId); // Implement this method in your DatabaseHelper

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                balance = cursor.getDouble(cursor.getColumnIndex("user_money")); // Replace "user_money" with your actual column name
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