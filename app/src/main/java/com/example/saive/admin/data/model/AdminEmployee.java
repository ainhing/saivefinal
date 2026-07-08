package com.example.saive.admin.data.model;

import com.google.firebase.database.PropertyName;

public class AdminEmployee {
    private String employeeId; // Firebase node key

    @PropertyName("FullName")
    private String fullName;

    @PropertyName("Email")
    private String email;

    @PropertyName("Phone")
    private String phone;

    @PropertyName("Department")
    private String department;

    @PropertyName("Position")
    private String position;

    @PropertyName("IsActive")
    private boolean isActive = true;

    @PropertyName("CreatedAt")
    private String createdAt;

    public AdminEmployee() {
        // Required for Firebase
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @PropertyName("FullName")
    public String getFullName() {
        return fullName;
    }

    @PropertyName("FullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @PropertyName("Email")
    public String getEmail() {
        return email;
    }

    @PropertyName("Email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("Phone")
    public String getPhone() {
        return phone;
    }

    @PropertyName("Phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @PropertyName("Department")
    public String getDepartment() {
        return department;
    }

    @PropertyName("Department")
    public void setDepartment(String department) {
        this.department = department;
    }

    @PropertyName("Position")
    public String getPosition() {
        return position;
    }

    @PropertyName("Position")
    public void setPosition(String position) {
        this.position = position;
    }

    @PropertyName("IsActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("IsActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    @PropertyName("CreatedAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @PropertyName("CreatedAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
