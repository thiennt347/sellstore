package com.sellproducts.thiennt.sellstore.model;

public class Rating {
    private  String userPhone;
    private  String productId;
    private  String rateValue;
    private  String comMent;

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComMent() {
        return comMent;
    }

    public void setComMent(String comMent) {
        this.comMent = comMent;
    }

    public Rating() {
    }

    public Rating(String userPhone, String productId, String rateValue, String comMent) {
        this.userPhone = userPhone;
        this.productId = productId;
        this.rateValue = rateValue;
        this.comMent = comMent;
    }
}
