package com.enterprise.catering.model;


public class User {
    private int id;
    private String name;
    private String loginName;
    private String password;
    private String phone;
    private String department;
    private String workstation;
    private String role;

    public User() {
    }

    public User(String name, String loginName, String password, String phone,
                String department, String workstation, String role) {
        this.name = name;
        this.loginName = loginName;
        this.password = password;
        this.phone = phone;
        this.department = department;
        this.workstation = workstation;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getWorkstation() {
        return workstation;
    }

    public void setWorkstation(String workstation) {
        this.workstation = workstation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}


