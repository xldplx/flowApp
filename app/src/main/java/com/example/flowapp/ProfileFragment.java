package com.example.flowapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView userId;
    private TextView accountType;
    private TextView email;
    private TextView phoneNumber;
    private TextView joinDate;
    private Button btnEditProfile;
    private Button btnSecuritySettings;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        userId = view.findViewById(R.id.user_id);
        accountType = view.findViewById(R.id.account_type);
        email = view.findViewById(R.id.email);
        phoneNumber = view.findViewById(R.id.phone_number);
        joinDate = view.findViewById(R.id.join_date);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnSecuritySettings = view.findViewById(R.id.btn_security_settings);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Set user data
        setUserData();

        // Set up the logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser ();
            }
        });

        return view;
    }

    private void setUserData() {
        // Replace these values with actual user data retrieval logic
        int currentUserId = UserSession.getInstance(getContext()).getUserId();
        userId.setText("User ID: " + currentUserId);
        accountType.setText("Standard Account");
        email.setText("Email: -");
        phoneNumber.setText("Phone: -");
        joinDate.setText("Joined: -");
    }

    private void logoutUser () {
        // Clear user session
        UserSession.getInstance(getContext()).logout();

        // Redirect to the login screen
        Intent intent = new Intent(getActivity(), PinLoginActivity.class);
        startActivity(intent);
        getActivity().finish(); // Close the current activity
    }
}