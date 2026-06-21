package com.kavya.stealthpad.data.DataModel;

public class LoginRequestDTO {
    private String email;
    private String pass;

    public LoginRequestDTO(String em, String ps){
        this.email = em;
        this.pass = ps;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
