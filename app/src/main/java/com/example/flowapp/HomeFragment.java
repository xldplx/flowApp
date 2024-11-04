package com.example.flowapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvBalance;
    private DatabaseHelper databaseHelper;
    private RecyclerView transactionHistoryRecyclerView;
    private TransactionsAdapter transactionsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBalance = view.findViewById(R.id.balance);
        databaseHelper = new DatabaseHelper(getActivity());

        // Check if the user is logged in
        if (!UserSession.getInstance(getContext()).isLoggedIn()) {
            navigateToLogin();
        } else {
            displayUserBalance();
            fetchAndDisplayTransactionHistory(view); // Pass the view to fetch transactions
        }

        // Set up buttons
        setupButtons(view);

        return view;
    }

    private void setupButtons(View view) {
        Button btnTopUp = view.findViewById(R.id.btn_top_up);
        btnTopUp.setOnClickListener(v -> navigateToTopupFragment());

        Button btnSend = view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> navigateToPayFragment());

        Button btnRequest = view.findViewById(R.id.btn_request);
        btnRequest.setOnClickListener(v -> Toast.makeText(getActivity(), "Coming soon", Toast.LENGTH_SHORT).show());

        Button btnMore = view.findViewById(R.id.btn_more);
        btnMore.setOnClickListener(v -> Toast.makeText(getActivity(), "Coming soon", Toast.LENGTH_SHORT).show());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), PinLoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void navigateToTopupFragment() {
        TopupFragment topupFragment = new TopupFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, topupFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToPayFragment() {
        PayFragment payFragment = new PayFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, payFragment)
                .addToBackStack(null)
                .commit();
    }

    private void displayUserBalance() {
        int userId = UserSession.getInstance(getContext()).getUserId();
        if (userId != -1) {
            double balance = getUserBalance(userId);
            String formattedBalance = formatToRupiah(balance);
            tvBalance.setText(formattedBalance);
        } else {
            tvBalance.setText("Error retrieving balance");
        }
    }

    private double getUserBalance(int userId) {
        return databaseHelper.getUserBalance(userId);
    }

    private String formatToRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    private void fetchAndDisplayTransactionHistory(View view) {
        int userId = UserSession.getInstance(getContext()).getUserId();
        if (userId != -1) {
            List<Transaction> transactions = databaseHelper.getUserTransactions(userId);

            // Set up the RecyclerView for transaction history
            transactionHistoryRecyclerView = view.findViewById(R.id.transaction_history_recycler);
            transactionsAdapter = new TransactionsAdapter(getContext(), transactions);
            transactionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            transactionHistoryRecyclerView.setAdapter(transactionsAdapter);
        } else {
            Toast.makeText(getActivity(), "Error fetching transaction history", Toast.LENGTH_SHORT).show();
        }
    }
}