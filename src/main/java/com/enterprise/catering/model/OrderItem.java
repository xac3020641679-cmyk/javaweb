package com.enterprise.catering.model;

public class OrderItem {
    private Integer id;
    private Integer orderId;
    private Integer menuItemId;
    private String name;
    private String unit;
    private Integer quantity;
    private Double price;
    private Double subtotal;

    public OrderItem() {
    }

    public OrderItem(Integer orderId, Integer menuItemId, String name, String unit,
                     Integer quantity, Double price, Double subtotal) {
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Integer menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}







