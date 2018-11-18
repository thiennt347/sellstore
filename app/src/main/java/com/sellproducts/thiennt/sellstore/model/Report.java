package com.sellproducts.thiennt.sellstore.model;

import java.util.List;

public class Report {
    private String phone;
    private String address;
    private String total;

    public Report() {
    }

    public Report(String phone, String address, String total) {
        this.phone = phone;
        this.address = address;
        this.total = total;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
