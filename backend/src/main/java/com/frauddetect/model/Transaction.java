package com.frauddetect.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction model representing a financial transaction to be analyzed
 */
public class Transaction {

    private String transactionId;
    private String userId;
    private String accountNumber;
    private double amount;
    private String currency;
    private String merchantName;
    private String merchantCategory;
    private String country;
    private String city;
    private String transactionType; // CREDIT, DEBIT, TRANSFER, WITHDRAWAL
    private String ipAddress;
    private String deviceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Fraud Analysis Results (populated after analysis)
    private double fraudScore;          // 0-100
    private String riskLevel;           // LOW, MEDIUM, HIGH, CRITICAL
    private String fraudReason;
    private boolean flagged;
    private String[] triggeredRules;

    // Constructors
    public Transaction() {
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now();
        this.currency = "USD";
    }

    public Transaction(String userId, String accountNumber, double amount, String merchantName,
                       String merchantCategory, String country, String city, String transactionType) {
        this();
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
        this.country = country;
        this.city = city;
        this.transactionType = transactionType;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getMerchantCategory() { return merchantCategory; }
    public void setMerchantCategory(String merchantCategory) { this.merchantCategory = merchantCategory; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getFraudScore() { return fraudScore; }
    public void setFraudScore(double fraudScore) { this.fraudScore = fraudScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getFraudReason() { return fraudReason; }
    public void setFraudReason(String fraudReason) { this.fraudReason = fraudReason; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public String[] getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(String[] triggeredRules) { this.triggeredRules = triggeredRules; }
}
