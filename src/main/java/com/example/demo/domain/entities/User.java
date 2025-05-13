package com.example.demo.domain.entities;

public class User {

    private String UserID;
    private String Name;
    private String Alias;
    private String Password;
    private String Email;
    private String Number;
    private String address;

    public User(String UserID, String Name, String Alias, String Password, String Email, String Number, String address) {
        this.UserID = UserID;
        this.Name = Name;
        this.Password = Password;
        this.Email = Email;
        this.Number = Number;
        this.address = address;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAlias(String alias) {
        Alias = alias;
    }

    public String getAlias() {
        return Alias;
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

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
