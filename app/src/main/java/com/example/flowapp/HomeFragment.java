package com.example.flowapp;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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

        QuickActionAdapter adapter = new QuickActionAdapter(quickActions);
        quickActionsRecycler.setAdapter(adapter);

        return view;
    }
}