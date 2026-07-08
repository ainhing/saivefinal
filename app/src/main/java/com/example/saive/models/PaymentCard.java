package com.example.saive.models;

public class PaymentCard {
    private String id;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cardType; // e.g., "Visa", "MasterCard"

    public PaymentCard(String id, String cardNumber, String cardHolderName, String expiryDate, String cardType) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cardType = cardType;
    }

    public String getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public String getExpiryDate() { return expiryDate; }
    public String getCardType() { return cardType; }

    public String getMaskedNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return "**** **** **** ****";
    }
}
