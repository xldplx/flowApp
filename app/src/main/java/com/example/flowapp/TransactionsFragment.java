package com.example.flowapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionsFragment extends Fragment {

    private RecyclerView transactionsRecyclerView;
    private TransactionsAdapter transactionsAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        transactionsRecyclerView = view.findViewById(R.id.transactions_recycler);
        databaseHelper = new DatabaseHelper(getActivity());

        // Set up RecyclerView
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionsAdapter = new TransactionsAdapter(getContext(), new ArrayList<>());
        transactionsRecyclerView.setAdapter(transactionsAdapter);

        // Load transactions for the current user
        loadUserTransactions(view); // Pass the view to the method

        return view;
    }

    private void loadUserTransactions(View view) {
        int userId = UserSession.getInstance(getActivity()).getUserId(); // Ensure this method returns a valid user ID

        // Fetch transactions for the user
        List<Transaction> transactions = databaseHelper.getUserTransactions(userId);

        // Check if transactions list is null
        if (transactions == null) {
            transactions = new ArrayList<>(); // Initialize to an empty list if null
        }

        // Calculate the total amount spent
        double totalSpent = 0.0;
        for (Transaction transaction : transactions) {
            if (transaction != null) { // Ensure transaction is not null
                totalSpent += transaction.getAmount(); // Sum up the amounts
            }
        }

        // Format the total amount spent in Indonesian Rupiah
        String formattedTotalSpent = formatToRupiah(totalSpent);

        // Update the total spent TextView
        TextView totalSpentTextView = view.findViewById(R.id.total_spent); // Use the passed view
        totalSpentTextView.setText(formattedTotalSpent); // Display formatted amount

        // Update the transactions in the adapter
        transactionsAdapter.updateTransactions(transactions);
    }

    private String formatToRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }
}