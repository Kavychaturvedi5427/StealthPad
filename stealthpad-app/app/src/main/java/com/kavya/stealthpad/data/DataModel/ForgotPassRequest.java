package com.kavya.stealthpad.data.DataModel;

public class ForgotPassRequest {

    private String email;

    public ForgotPassRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}