package com.kavya.stealthpad.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "stealth_auth";

    private final SharedPreferences prefs;

    public SessionManager(Context context){
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // storing the data in the temporary storage...
    public void saveUser(String jwt, String name, String email) {
        prefs.edit()
                .putString("jwt", jwt)
                .putString("name", name)
                .putString("email", email)
                .apply();
    }

    public boolean isLoggedIn(){
        String jwt = getToken();
        return jwt != null  && !jwt.isEmpty();
    }

    public void logout(){
        // Only clear session-related data
        prefs.edit()
                .remove("jwt")
                .remove("name")
                .remove("email")
                .putBoolean("is_logging_out", false)
                .apply();
    }


    public String getToken() {
        return prefs.getString("jwt", null);
    }

    public String getName() {
        return prefs.getString("name", null);
    }

    public String getEmail() {
        return prefs.getString("email", null);
    }

    // Vault methods
    public boolean isVaultSetup() {
        return prefs.getBoolean("vault_setup", false);
    }

    public void setVaultSetup(boolean setup) {
        prefs.edit().putBoolean("vault_setup", setup).apply();
    }

    public String getVaultPin() {
        return prefs.getString("vault_pin", null);
    }

    public void setVaultPin(String pin) {
        prefs.edit().putString("vault_pin", pin).apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean("biometric_enabled", false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply();
    }

    // Theme methods
    public int getThemeMode() {
        return prefs.getInt("theme_mode", 0); // 0: System, 1: Light, 2: Dark
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt("theme_mode", mode).apply();
    }

    // Auto Lock methods
    public int getAutoLockMinutes() {
        return prefs.getInt("auto_lock_minutes", 0); // 0: Immediately, 1, 5, 15
    }

    public void setAutoLockMinutes(int minutes) {
        prefs.edit().putInt("auto_lock_minutes", minutes).apply();
    }

    // Sorting methods
    public String getSortOrder() {
        return prefs.getString("sort_order", "RECENTLY_UPDATED");
    }

    public void setSortOrder(String sortOrder) {
        prefs.edit().putString("sort_order", sortOrder).apply();
    }

    // Default Category methods
    public String getDefaultCategory() {
        return prefs.getString("default_category", "Personal");
    }

    public void setDefaultCategory(String category) {
        prefs.edit().putString("default_category", category).apply();
    }

    public boolean isLoggingOut() {
        return prefs.getBoolean("is_logging_out", false);
    }

    public void setLoggingOut(boolean loggingOut) {
        prefs.edit().putBoolean("is_logging_out", loggingOut).apply();
    }
}
