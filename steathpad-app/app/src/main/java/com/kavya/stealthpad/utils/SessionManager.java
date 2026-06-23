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


    public String getToken() {
        return prefs.getString("jwt", null);
    }

    public String getName() {
        return prefs.getString("name", null);
    }

    public String getEmail() {
        return prefs.getString("email", null);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

}
