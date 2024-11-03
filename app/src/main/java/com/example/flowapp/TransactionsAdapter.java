package com.example.flowapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private Context context;

    public TransactionsAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.tvTransactionTitle.setText(transaction.getTitle());
        holder.tvTransactionDate.setText(transaction.getDate());
        holder.tvTransactionAmount.setText("Rp " + transaction.getAmount()); // Format the amount
        holder.ivTransactionType.setImageResource(transaction.getImageResourceId()); // Set the image resource
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionTitle;
        TextView tvTransactionDate;
        TextView tvTransactionAmount;
        ImageView ivTransactionType;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionTitle = itemView.findViewById(R.id.tv_transaction_title);
            tvTransactionDate = itemView.findViewById(R.id.tv_transaction_date);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
            ivTransactionType = itemView.findViewById(R.id.iv_transaction_type);
        }
    }
}