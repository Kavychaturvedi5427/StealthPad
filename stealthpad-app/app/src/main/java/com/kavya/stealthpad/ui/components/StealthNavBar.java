package com.kavya.stealthpad.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.kavya.stealthpad.R;

public class StealthNavBar extends MaterialCardView {

    private View navHome, navCategories, navVault, navMore;
    private View navAdd;
    private OnNavigationItemSelectedListener listener;

    public interface OnNavigationItemSelectedListener {
        void onItemSelected(int itemId);
    }

    public StealthNavBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public StealthNavBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StealthNavBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_stealth_nav_bar, this, true);

        setRadius(getResources().getDimension(R.dimen.nav_bar_radius));
        setCardElevation(getResources().getDimension(R.dimen.nav_bar_elevation));
        setCardBackgroundColor(context.getColor(R.color.dash_card_bg));

        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navVault = findViewById(R.id.nav_vault);
        navMore = findViewById(R.id.nav_more);
        navAdd = findViewById(R.id.nav_add);

        navHome.setOnClickListener(v -> handleSelection(R.id.nav_home));
        navCategories.setOnClickListener(v -> handleSelection(R.id.nav_categories));
        navVault.setOnClickListener(v -> handleSelection(R.id.nav_vault));
        navMore.setOnClickListener(v -> handleSelection(R.id.nav_more));
        
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> {
                if (listener != null) listener.onItemSelected(R.id.nav_add);
            });
        }

        // Default selection
        setSelected(R.id.nav_home);
    }

    public void setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
        this.listener = listener;
    }

    private void handleSelection(int itemId) {
        setSelected(itemId);
        if (listener != null) {
            listener.onItemSelected(itemId);
        }
    }

    public void setSelected(int itemId) {
        if (navHome != null) navHome.setSelected(itemId == R.id.nav_home);
        if (navCategories != null) navCategories.setSelected(itemId == R.id.nav_categories);
        if (navVault != null) navVault.setSelected(itemId == R.id.nav_vault);
        if (navMore != null) navMore.setSelected(itemId == R.id.nav_more);
    }
}
