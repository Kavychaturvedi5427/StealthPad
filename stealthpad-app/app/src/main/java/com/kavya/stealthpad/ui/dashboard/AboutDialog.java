package com.kavya.stealthpad.ui.dashboard;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.kavya.stealthpad.R;

public class AboutDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.AboutDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_about, container, false);
        
        // Setup Close Button
        MaterialButton closeBtn = view.findViewById(R.id.btn_close_about);
        closeBtn.setOnClickListener(v -> dismiss());

        // Setup Dynamic Version
        TextView versionText = view.findViewById(R.id.about_version);
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            versionText.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("1.0.0");
        }

        // Setup Social Links
        view.findViewById(R.id.link_linkedin).setOnClickListener(v -> openUrl("https://www.linkedin.com/in/kavyachaturvedi58/"));
        view.findViewById(R.id.link_github).setOnClickListener(v -> openUrl("https://github.com/Kavychaturvedi5427"));
        view.findViewById(R.id.link_instagram).setOnClickListener(v -> openUrl("https://www.instagram.com/kavy___17"));
        view.findViewById(R.id.link_portfolio).setOnClickListener(v -> openUrl("https://kavyadev.in"));

        return view;
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            // Handle cases where no browser is available
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }
}
