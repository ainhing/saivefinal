package com.example.saive.models;

import java.io.Serializable;

public class Address implements Serializable {
    private String id;
    private String label; // Home, Office, etc.
    private String fullName;
    private String phoneNumber;
    private String streetAddress;
    private String ward;
    private String city;
    private String district;
    private String country;
    private boolean isDefault;

    public Address(String id, String label, String fullName, String phoneNumber, String streetAddress, String ward, String district, String city, boolean isDefault) {
        this.id = id;
        this.label = label;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.isDefault = isDefault;
        this.country = "Vietnam"; // Default
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getStreetAddress() { return streetAddress; }
    public String getWard() { return ward; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getCountry() { return country; }
    public boolean isDefault() { return isDefault; }

    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public void setCountry(String country) { this.country = country; }

    public String getFullDisplayAddress() {
        return streetAddress + ", " + ward + ", " + district + ", " + city + (country != null ? ", " + country : "");
    }
}