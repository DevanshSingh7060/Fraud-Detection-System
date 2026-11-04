package com.frauddetect.engine;

import com.frauddetect.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ML Fraud Detection Engine
 *
 * Implements a multi-factor fraud scoring system simulating real ML techniques:
 *
 * 1. VELOCITY CHECK        - Rapid repeated transactions (simulated pattern detection)
 * 2. AMOUNT ANOMALY        - Statistical deviation from baseline (Z-score simulation)
 * 3. GEO-LOCATION MISMATCH - Country/region risk scoring
 * 4. MERCHANT CATEGORY RISK- High-risk merchant classification
 * 5. TIME-OF-DAY ANALYSIS  - Unusual hours detection
 * 6. ROUND AMOUNT PATTERN  - Structured transactions (money laundering indicator)
 * 7. NEW DEVICE/IP SCORE   - Device fingerprint anomaly
 *
 * Final score is a weighted ensemble of all factors (0-100).
 */
@Component
public class MLFraudEngine {

    // High-risk merchant categories (weighted risk map)
    private static final Map<String, Double> MERCHANT_RISK_SCORES = new HashMap<>() {{
        put("CRYPTOCURRENCY", 0.85);
        put("GAMBLING", 0.80);
        put("ADULT_CONTENT", 0.75);
        put("MONEY_TRANSFER", 0.70);
        put("PAWN_SHOP", 0.65);
        put("JEWELRY", 0.55);
        put("ELECTRONICS", 0.45);
        put("ONLINE_GAMING", 0.40);
        put("TRAVEL", 0.25);
        put("RESTAURANT", 0.10);
        put("GROCERY", 0.05);
        put("PHARMACY", 0.05);
        put("UTILITIES", 0.05);
    }};

    // High-risk countries (simplified geo-risk model)
    private static final Map<String, Double> COUNTRY_RISK_SCORES = new HashMap<>() {{
        put("NG", 0.90); // Nigeria
        put("RO", 0.75); // Romania
        put("RU", 0.70); // Russia
        put("UA", 0.65); // Ukraine
        put("BR", 0.55); // Brazil
        put("CN", 0.50); // China
        put("IN", 0.20);
        put("US", 0.10);
        put("GB", 0.08);
        put("DE", 0.08);
        put("AU", 0.08);
        put("CA", 0.08);
        put("JP", 0.05);
        put("SG", 0.05);
    }};

    // Simulated user behavioral baselines (avg monthly spend per user profile)
    private static final Map<String, Double> USER_BASELINES = new HashMap<>() {{
        put("USR001", 450.0);
        put("USR002", 1200.0);
        put("USR003", 800.0);
        put("USR004", 2500.0);
        put("USR005", 300.0);
    }};

    // Transaction velocity tracker (userId -> list of recent timestamps)
    private final Map<String, List<LocalDateTime>> velocityTracker = new HashMap<>();

    /**
     * Main analysis method - computes fraud score using ensemble scoring
     *
     * @param transaction The transaction to analyze
     * @return Analyzed transaction with fraud score and risk level
     */
    public Transaction analyze(Transaction transaction) {
        List<String> triggeredRules = new ArrayList<>();
        double totalScore = 0.0;

        // --- Rule 1: Amount Anomaly Detection (Weight: 25%) ---
        double amountScore = analyzeAmountAnomaly(transaction, triggeredRules);
        totalScore += amountScore * 0.25;

        // --- Rule 2: Merchant Category Risk (Weight: 20%) ---
        double merchantScore = analyzeMerchantRisk(transaction, triggeredRules);
        totalScore += merchantScore * 0.20;

        // --- Rule 3: Geographic Risk Analysis (Weight: 20%) ---
        double geoScore = analyzeGeoRisk(transaction, triggeredRules);
        totalScore += geoScore * 0.20;

        // --- Rule 4: Velocity Check (Weight: 20%) ---
        double velocityScore = analyzeVelocity(transaction, triggeredRules);
        totalScore += velocityScore * 0.20;

        // --- Rule 5: Time-of-Day Analysis (Weight: 10%) ---
        double timeScore = analyzeTimeOfDay(transaction, triggeredRules);
        totalScore += timeScore * 0.10;

        // --- Rule 6: Round Amount Pattern (Weight: 5%) ---
        double roundAmountScore = analyzeRoundAmount(transaction, triggeredRules);
        totalScore += roundAmountScore * 0.05;

        // Normalize to 0-100
        double finalScore = Math.min(totalScore * 100.0, 100.0);

        // Apply non-linear scaling for extreme cases
        if (finalScore > 85) finalScore = Math.min(finalScore * 1.05, 100.0);

        // Round to 2 decimal places
        finalScore = Math.round(finalScore * 100.0) / 100.0;

        // Set risk level based on score
        String riskLevel = computeRiskLevel(finalScore);

        // Populate transaction result fields
        transaction.setFraudScore(finalScore);
        transaction.setRiskLevel(riskLevel);
        transaction.setFlagged(finalScore >= 40.0);
        transaction.setTriggeredRules(triggeredRules.toArray(new String[0]));
        transaction.setFraudReason(buildFraudReason(triggeredRules, finalScore));

        return transaction;
    }

    /**
     * Analyze amount deviation from user's baseline using Z-score simulation
     */
    private double analyzeAmountAnomaly(Transaction txn, List<String> rules) {
        double amount = txn.getAmount();
        double baseline = USER_BASELINES.getOrDefault(txn.getUserId(), 600.0);

        // Simulate Z-score: how many std-devs away from mean
        double stdDev = baseline * 0.4; // assume 40% std dev
        double zScore = Math.abs(amount - baseline) / stdDev;

        if (amount > 10000) {
            rules.add("CRITICAL_AMOUNT: Transaction exceeds $10,000 (AML threshold)");
            return 0.95;
        } else if (zScore > 3.0) {
            rules.add("AMOUNT_ANOMALY: Amount is " + String.format("%.1f", zScore) + " std deviations above baseline");
            return Math.min(0.85, zScore / 5.0);
        } else if (zScore > 2.0) {
            rules.add("UNUSUAL_AMOUNT: Moderately high transaction relative to history");
            return 0.50;
        } else if (amount < 1.0) {
            rules.add("MICRO_TRANSACTION: Probe transaction pattern detected");
            return 0.60;
        }

        return Math.min(zScore / 4.0, 0.30);
    }

    /**
     * Score based on merchant category risk profile
     */
    private double analyzeMerchantRisk(Transaction txn, List<String> rules) {
        String category = txn.getMerchantCategory() != null
                ? txn.getMerchantCategory().toUpperCase()
                : "UNKNOWN";

        Double riskScore = MERCHANT_RISK_SCORES.get(category);
        if (riskScore == null) {
            riskScore = 0.30; // Unknown merchant = moderate risk
            rules.add("UNKNOWN_MERCHANT_CATEGORY: Merchant type not recognized");
        } else if (riskScore >= 0.70) {
            rules.add("HIGH_RISK_MERCHANT: Category '" + category + "' is flagged as high-risk");
        } else if (riskScore >= 0.40) {
            rules.add("MEDIUM_RISK_MERCHANT: Category '" + category + "' has elevated risk profile");
        }

        return riskScore;
    }

    /**
     * Geographic risk analysis using country risk scores
     */
    private double analyzeGeoRisk(Transaction txn, List<String> rules) {
        String country = txn.getCountry() != null ? txn.getCountry().toUpperCase() : "XX";
        Double geoRisk = COUNTRY_RISK_SCORES.get(country);

        if (geoRisk == null) {
            geoRisk = 0.45;
            rules.add("UNKNOWN_GEOGRAPHY: Transaction origin country unrecognized");
        } else if (geoRisk >= 0.70) {
            rules.add("HIGH_RISK_COUNTRY: Transaction from high-fraud-rate country (" + country + ")");
        } else if (geoRisk >= 0.40) {
            rules.add("ELEVATED_RISK_REGION: Transaction from moderate-risk region (" + country + ")");
        }

        return geoRisk;
    }

    /**
     * Transaction velocity check - detects multiple transactions in short time window
     */
    private double analyzeVelocity(Transaction txn, List<String> rules) {
        String userId = txn.getUserId();
        LocalDateTime now = txn.getTimestamp() != null ? txn.getTimestamp() : LocalDateTime.now();

        List<LocalDateTime> timestamps = velocityTracker.computeIfAbsent(userId, k -> new ArrayList<>());

        // Count transactions in last 5 minutes (simulated window)
        long recentCount = timestamps.stream()
                .filter(t -> t.isAfter(now.minusMinutes(5)))
                .count();

        timestamps.add(now);

        // Keep only last 50 entries
        if (timestamps.size() > 50) {
            timestamps.remove(0);
        }

        if (recentCount >= 5) {
            rules.add("HIGH_VELOCITY: " + (recentCount + 1) + " transactions in <5 min window");
            return 0.90;
        } else if (recentCount >= 3) {
            rules.add("ELEVATED_VELOCITY: Multiple transactions detected in short timeframe");
            return 0.60;
        } else if (recentCount >= 2) {
            rules.add("VELOCITY_WARNING: Repeated transactions within 5-minute window");
            return 0.35;
        }

        return 0.05;
    }

    /**
     * Time-of-day analysis - late night transactions are riskier
     */
    private double analyzeTimeOfDay(Transaction txn, List<String> rules) {
        int hour = txn.getTimestamp() != null ? txn.getTimestamp().getHour() : LocalDateTime.now().getHour();

        if (hour >= 1 && hour <= 4) {
            rules.add("SUSPICIOUS_HOUR: Transaction at " + hour + ":00 AM (high-risk window 1-4 AM)");
            return 0.80;
        } else if (hour == 0 || hour == 5) {
            rules.add("OFF_HOURS: Transaction outside normal banking hours");
            return 0.50;
        } else if (hour >= 22 || hour <= 6) {
            rules.add("LATE_NIGHT: Transaction during low-activity period");
            return 0.30;
        }

        return 0.05; // Normal business hours
    }

    /**
     * Detect round-number transactions (common in structured money laundering)
     */
    private double analyzeRoundAmount(Transaction txn, List<String> rules) {
        double amount = txn.getAmount();

        // Check if amount is a round number (no cents)
        if (amount >= 1000 && amount % 1000 == 0) {
            rules.add("ROUND_AMOUNT: Perfectly round large amount ($" + (int) amount + ") - structuring indicator");
            return 0.70;
        } else if (amount >= 100 && amount % 100 == 0) {
            rules.add("ROUND_AMOUNT: Round hundred-dollar amount pattern detected");
            return 0.40;
        }

        return 0.05;
    }

    /**
     * Determine risk level category from score
     */
    private String computeRiskLevel(double score) {
        if (score >= 75) return "CRITICAL";
        if (score >= 55) return "HIGH";
        if (score >= 35) return "MEDIUM";
        return "LOW";
    }

    /**
     * Build human-readable fraud reason summary
     */
    private String buildFraudReason(List<String> rules, double score) {
        if (rules.isEmpty()) return "No suspicious patterns detected";
        if (score >= 75) return "CRITICAL FRAUD RISK: Multiple high-severity indicators detected — " + rules.get(0);
        if (score >= 55) return "HIGH FRAUD RISK: Significant anomalies found — " + rules.get(0);
        if (score >= 35) return "MEDIUM RISK: Suspicious patterns detected — " + rules.get(0);
        return "LOW RISK: Minor anomaly — " + rules.get(0);
    }
}
