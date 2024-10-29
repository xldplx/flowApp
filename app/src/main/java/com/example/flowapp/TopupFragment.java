package com.example.flowapp;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class TopupFragment extends Fragment {

    private EditText etTopup;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topup, container, false);

        etTopup = view.findViewById(R.id.et_topup);
        databaseHelper = new DatabaseHelper(getActivity());

        setupKeypad(view);
        setupTopupButton(view);
        setupEditTextListener();

        return view;
    }

    private void setupEditTextListener() {
        etTopup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String formatted = formatAmount(s.toString());
                if (!formatted.equals(s.toString())) {
                    etTopup.setText(formatted);
                    etTopup.setSelection(formatted.length()); // Move cursor to the end
                }
            }
        });
    }

    private String formatAmount(String amountStr) {
        // Remove non-numeric characters
        String cleanString = amountStr.replaceAll("[^\\d]", "");
        if (cleanString.isEmpty()) {
            return "";
        }

        // Parse the string to a number
        double amount = Double.parseDouble(cleanString);
        // Format the number with commas
        return NumberFormat.getInstance(Locale.US).format(amount);
    }

    private void setupKeypad(View view) {
        // Set up keypad buttons
        int[] buttonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
                R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
                R.id.btn_8, R.id.btn_9, R.id.btn_dot, R.id.btn_backspace
        };

        for (int id : buttonIds) {
            View button = view.findViewById(id);
            if (button instanceof Button) {
                button.setOnClickListener(v -> onKeypadButtonClick(((Button) button).getText().toString()));
            } else if (button instanceof ImageButton) {
                button.setOnClickListener(v -> onKeypadButtonClick(((ImageButton) button).getContentDescription().toString()));
            } else {
                Log.e("TopupFragment", "Button with ID " + id + " is not a Button or ImageButton");
            }
        }
    }

    private void onKeypadButtonClick(String value) {
        if (value.equals(".")) {
            String currentText = etTopup.getText().toString();
            if (!currentText.contains(".")) {
                etTopup.append(value);
            }
        } else if (value.equals("DELETE")) {
            String currentText = etTopup.getText().toString();
            if (currentText.length() > 0) {
                etTopup.setText(currentText.substring(0, currentText.length() - 1));
            }
        } else {
            etTopup.append(value);
        }
    }

    private void setupTopupButton(View view) {
        Button btnTopup = view.findViewById(R.id.btn_topup);
        if (btnTopup != null) {
            btnTopup.setOnClickListener(v -> performTopup());
        } else {
            Log.e("TopupFragment", "Topup button not found in layout");
        }
    }

    private void performTopup() {
        String amountStr = etTopup.getText().toString();
        if (!amountStr.isEmpty()) {
            double amount = Double.parseDouble(amountStr.replace(",", ""));
            int userId = 1; // Replace with actual user ID retrieval logic
            updateUserMoney(userId, amount);

            // Navigate back to HomeFragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void updateUserMoney(int userId, double amount) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_USERS + " SET " +
                DatabaseHelper.COLUMN_USER_MONEY + " = " +
                DatabaseHelper.COLUMN_USER_MONEY + " + ? WHERE " +
                DatabaseHelper.COLUMN_USER_ID + " = ?", new Object[]{amount, userId});
        db.close();
    }
}