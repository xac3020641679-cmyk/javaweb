package com.enterprise.catering.model;

import java.io.Serializable;
import java.util.Date;


public class BlanketOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private Date date;          // 订单日期
    private String itemName;    // 菜名
    private String unit;        // 单位
    private int totalQuantity;  // 总数量
    private double price;       // 单价
    private double subtotal;    // 小计

    public BlanketOrder() {}

    public BlanketOrder(Date date, String itemName, String unit, int totalQuantity, double price, double subtotal) {
        this.date = date;
        this.itemName = itemName;
        this.unit = unit;
        this.totalQuantity = totalQuantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}


