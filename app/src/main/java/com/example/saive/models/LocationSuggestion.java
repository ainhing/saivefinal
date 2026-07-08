package com.example.saive.models;

public class LocationSuggestion {
    private String placeId;
    private String mainText;
    private String secondaryText;

    public LocationSuggestion(String placeId, String mainText, String secondaryText) {
        this.placeId = placeId;
        this.mainText = mainText;
        this.secondaryText = secondaryText;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getMainText() {
        return mainText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }
}
