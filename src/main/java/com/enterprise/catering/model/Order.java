package com.enterprise.catering.model;

import java.util.Date;

public class Order {
    private Integer id;
    private Integer userId;
    private String userName;
    private String phone;
    private String workLocation;
    private Date orderTime;
    private Date mealDate;
    private Double totalPrice;
    private Date createdAt;

    public Order() {
    }

    public Order(Integer userId, String userName, String phone, Date mealDate, Double totalPrice) {
        this.userId = userId;
        this.userName = userName;
        this.phone = phone;
        this.mealDate = mealDate;
        this.totalPrice = totalPrice;
        this.orderTime = new Date();
        this.createdAt = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Date getMealDate() {
        return mealDate;
    }

    public void setMealDate(Date mealDate) {
        this.mealDate = mealDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}



