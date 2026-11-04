package com.frauddetect.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FraudAlert model - generated when a high-risk transaction is detected
 */
public class FraudAlert {

    private String alertId;
    private String transactionId;
    private String userId;
    private String alertType;       // VELOCITY, AMOUNT_ANOMALY, GEO_MISMATCH, HIGH_RISK_MERCHANT, TIME_ANOMALY
    private String severity;        // LOW, MEDIUM, HIGH, CRITICAL
    private String description;
    private double fraudScore;
    private String accountNumber;
    private double transactionAmount;
    private String currency;
    private boolean resolved;
    private String resolution;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alertTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedTime;

    public FraudAlert() {
        this.alertId = "ALT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.alertTime = LocalDateTime.now();
        this.resolved = false;
        this.currency = "USD";
    }

    public FraudAlert(Transaction transaction, String alertType, String description) {
        this();
        this.transactionId = transaction.getTransactionId();
        this.userId = transaction.getUserId();
        this.accountNumber = transaction.getAccountNumber();
        this.fraudScore = transaction.getFraudScore();
        this.severity = transaction.getRiskLevel();
        this.transactionAmount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.alertType = alertType;
        this.description = description;
    }

    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getFraudScore() { return fraudScore; }
    public void setFraudScore(double fraudScore) { this.fraudScore = fraudScore; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public double getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(double transactionAmount) { this.transactionAmount = transactionAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }

    public LocalDateTime getResolvedTime() { return resolvedTime; }
    public void setResolvedTime(LocalDateTime resolvedTime) { this.resolvedTime = resolvedTime; }
}
