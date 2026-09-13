package com.kavya.stealthpad.data.DataModel;

public class LoginRequestDTO {
    private String email;
    private String pass;

    public LoginRequestDTO(String email, String password){
        this.email = email;
        this.pass = password;
    }

    public String getPassword() {
        return pass;
    }

    public void setPassword(String password) {
        this.pass = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
