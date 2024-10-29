package com.example.flowapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuickActionAdapter extends RecyclerView.Adapter<QuickActionAdapter.ViewHolder> {

    private List<QuickAction> quickActions;
    private Context context;

    public QuickActionAdapter(List<QuickAction> quickActions, Context context) {
        this.quickActions = quickActions;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_action, parent, false);
        return new ViewHolder(view);
    }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QuickAction action = quickActions.get(position);
            holder.icon.setImageResource(action.getIconResId());
            holder.title.setText(action.getTitle());

            // Set click listener for the item
            holder.itemView.setOnClickListener(v -> {
                if (action.getTitle().equals("Scan")) {
                    Intent intent = new Intent(context, QRScannerActivity.class);
                    context.startActivity(intent);
                } else if (action.getTitle().equals("Wallet")) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).navigateToFragment(new WalletFragment());
                    }
                } else if (action.getTitle().equals("Pay")) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).navigateToFragment(new PayFragment());
                    }
                }
            });

        }

    @Override
    public int getItemCount() {
        return quickActions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
        }
    }
}