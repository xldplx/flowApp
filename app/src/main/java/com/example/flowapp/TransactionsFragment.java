package com.example.flowapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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
        loadUserTransactions();

        return view;
    }

    private void loadUserTransactions() {
        int userId = UserSession.getInstance(getActivity()).getUserId();
        List<Transaction> transactions = databaseHelper.getUserTransactions(userId); // Implement this in DatabaseHelper
        transactionsAdapter.updateTransactions(transactions);
    }
}