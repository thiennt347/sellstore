package com.sellproducts.thiennt.sellstore.model;

public class User {
    private  String Name;
    private  String Password;
    private String Phone;
    private  String IsStaff;
    private String HomeAddress;
    private  String Email;


    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getHomeAddress() {
        return HomeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        HomeAddress = homeAddress;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public User() {
    }

    public User(String name, String password) {
        Name = name;
        Password = password;
        IsStaff = "false";
    }
}
