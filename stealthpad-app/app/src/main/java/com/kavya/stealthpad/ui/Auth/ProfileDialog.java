package com.kavya.stealthpad.ui.Auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.se.omapi.Session;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ui.dashboard.DashboardActivity;
import com.kavya.stealthpad.utils.SessionManager;


public class ProfileDialog extends DialogFragment {

    private LinearLayout logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView username = view.findViewById(R.id.text_full_name);
        TextView email = view.findViewById(R.id.text_email);
        SessionManager sessionManager = new SessionManager(requireContext());

        username.setText(sessionManager.getName());
        email.setText(sessionManager.getEmail());
        
        view.findViewById(R.id.btn_close_profile).setOnClickListener(v -> dismiss());
        
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {

            // confirmation for logout...
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        sessionManager.logout();
                        // clear the activity stack and redirect to the dashboard...
                        Intent intent = new Intent(requireContext(), DashboardActivity.class);
                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );
                        startActivity(intent);
                        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        dismiss();

                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null && getDialog().getWindow() != null){
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
