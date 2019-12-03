package com.example.trabajofinalobjetos15_7_2019.DTOs;

import com.google.gson.annotations.SerializedName;

public class UserDTO {

    @SerializedName("userName")
    private String UserName ;
    @SerializedName("password")
    private String Password;
    @SerializedName("email")
    private String Email;
    @SerializedName("token")
    private String Token;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }


    public UserDTO(String UserName, String Password , String Email) {
        this.UserName = UserName;
        this.Password = Password;
        this.Email = Email;
    }


}
