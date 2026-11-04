package com.frauddetect.service;

import com.frauddetect.engine.MLFraudEngine;
import com.frauddetect.model.FraudAlert;
import com.frauddetect.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Transaction Service
 *
 * Orchestrates transaction analysis using the MLFraudEngine and manages
 * in-memory storage of transactions and fraud alerts.
 */
@Service
public class TransactionService {

    private final MLFraudEngine fraudEngine;

    // In-memory transaction store (production would use a database)
    private final List<Transaction> transactionStore = new CopyOnWriteArrayList<>();
    private final List<FraudAlert> alertStore = new CopyOnWriteArrayList<>();

    @Autowired
    public TransactionService(MLFraudEngine fraudEngine) {
        this.fraudEngine = fraudEngine;
        loadDemoData();
    }

    /**
     * Analyze a new transaction through the ML Fraud Engine
     */
    public Transaction analyzeTransaction(Transaction transaction) {
        // Run ML fraud detection
        Transaction analyzed = fraudEngine.analyze(transaction);

        // Store the transaction
        transactionStore.add(0, analyzed);

        // Generate alert if flagged
        if (analyzed.isFlagged()) {
            FraudAlert alert = buildAlert(analyzed);
            alertStore.add(0, alert);
        }

        return analyzed;
    }

    /**
     * Get all transactions (most recent first)
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactionStore);
    }

    /**
     * Get all fraud alerts (most recent first)
     */
    public List<FraudAlert> getAllAlerts() {
        return new ArrayList<>(alertStore);
    }

    /**
     * Get only unresolved alerts
     */
    public List<FraudAlert> getActiveAlerts() {
        return alertStore.stream()
                .filter(a -> !a.isResolved())
                .collect(Collectors.toList());
    }

    /**
     * Resolve an alert by ID
     */
    public Optional<FraudAlert> resolveAlert(String alertId, String resolution) {
        return alertStore.stream()
                .filter(a -> a.getAlertId().equals(alertId))
                .findFirst()
                .map(alert -> {
                    alert.setResolved(true);
                    alert.setResolution(resolution);
                    alert.setResolvedTime(LocalDateTime.now());
                    return alert;
                });
    }

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        int total = transactionStore.size();
        long flagged = transactionStore.stream().filter(Transaction::isFlagged).count();
        long critical = transactionStore.stream().filter(t -> "CRITICAL".equals(t.getRiskLevel())).count();
        long high = transactionStore.stream().filter(t -> "HIGH".equals(t.getRiskLevel())).count();
        long medium = transactionStore.stream().filter(t -> "MEDIUM".equals(t.getRiskLevel())).count();
        long low = transactionStore.stream().filter(t -> "LOW".equals(t.getRiskLevel())).count();

        double totalAmount = transactionStore.stream().mapToDouble(Transaction::getAmount).sum();
        double avgFraudScore = transactionStore.stream().mapToDouble(Transaction::getFraudScore).average().orElse(0.0);

        // Blocked amount (potential losses prevented)
        double blockedAmount = transactionStore.stream()
                .filter(t -> t.getRiskLevel().equals("CRITICAL") || t.getRiskLevel().equals("HIGH"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        stats.put("totalTransactions", total);
        stats.put("flaggedTransactions", flagged);
        stats.put("criticalRisk", critical);
        stats.put("highRisk", high);
        stats.put("mediumRisk", medium);
        stats.put("lowRisk", low);
        stats.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
        stats.put("averageFraudScore", Math.round(avgFraudScore * 100.0) / 100.0);
        stats.put("blockedAmount", Math.round(blockedAmount * 100.0) / 100.0);
        stats.put("fraudRate", total > 0 ? Math.round((flagged * 10000.0 / total)) / 100.0 : 0.0);
        stats.put("activeAlerts", alertStore.stream().filter(a -> !a.isResolved()).count());
        stats.put("resolvedAlerts", alertStore.stream().filter(FraudAlert::isResolved).count());

        // Risk distribution chart data
        Map<String, Long> riskDistribution = new LinkedHashMap<>();
        riskDistribution.put("CRITICAL", critical);
        riskDistribution.put("HIGH", high);
        riskDistribution.put("MEDIUM", medium);
        riskDistribution.put("LOW", low);
        stats.put("riskDistribution", riskDistribution);

        // Recent fraud scores for trend chart (last 20)
        List<Map<String, Object>> recentScores = transactionStore.stream()
                .limit(20)
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getTransactionId());
                    m.put("score", t.getFraudScore());
                    m.put("risk", t.getRiskLevel());
                    m.put("amount", t.getAmount());
                    m.put("timestamp", t.getTimestamp() != null ? t.getTimestamp().toString() : "");
                    return m;
                })
                .collect(Collectors.toList());
        stats.put("recentScores", recentScores);

        return stats;
    }

    /**
     * Build a FraudAlert from a flagged transaction
     */
    private FraudAlert buildAlert(Transaction txn) {
        String alertType = determineAlertType(txn.getTriggeredRules());
        String description = txn.getFraudReason();
        return new FraudAlert(txn, alertType, description);
    }

    private String determineAlertType(String[] rules) {
        if (rules == null || rules.length == 0) return "GENERAL_FRAUD";
        String firstRule = rules[0].toUpperCase();
        if (firstRule.contains("VELOCITY")) return "VELOCITY";
        if (firstRule.contains("AMOUNT")) return "AMOUNT_ANOMALY";
        if (firstRule.contains("COUNTRY") || firstRule.contains("GEO")) return "GEO_MISMATCH";
        if (firstRule.contains("MERCHANT")) return "HIGH_RISK_MERCHANT";
        if (firstRule.contains("HOUR") || firstRule.contains("NIGHT")) return "TIME_ANOMALY";
        return "BEHAVIORAL_ANOMALY";
    }

    /**
     * Load pre-seeded demo transaction data for initial dashboard visualization
     */
    private void loadDemoData() {
        List<Transaction> demos = Arrays.asList(
            makeDemo("USR001", "ACC-4521-****-7890", 15000.00, "Crypto Exchange Pro", "CRYPTOCURRENCY", "RU", "Moscow", "DEBIT"),
            makeDemo("USR002", "ACC-7834-****-2341", 250.00, "Walmart", "GROCERY", "US", "New York", "DEBIT"),
            makeDemo("USR003", "ACC-1122-****-9900", 8500.00, "Luxury Jewels Ltd", "JEWELRY", "NG", "Lagos", "TRANSFER"),
            makeDemo("USR001", "ACC-4521-****-7890", 500.00, "Amazon", "ELECTRONICS", "US", "Seattle", "DEBIT"),
            makeDemo("USR004", "ACC-5566-****-3344", 1000.00, "Lucky Casino Online", "GAMBLING", "RO", "Bucharest", "DEBIT"),
            makeDemo("USR005", "ACC-9988-****-1122", 45.00, "Starbucks", "RESTAURANT", "US", "Chicago", "DEBIT"),
            makeDemo("USR002", "ACC-7834-****-2341", 3200.00, "MoneyGram Transfer", "MONEY_TRANSFER", "UA", "Kiev", "TRANSFER"),
            makeDemo("USR003", "ACC-1122-****-9900", 89.99, "Netflix", "ONLINE_GAMING", "US", "Los Angeles", "DEBIT"),
            makeDemo("USR004", "ACC-5566-****-3344", 500.00, "BitcoinMart", "CRYPTOCURRENCY", "CN", "Shanghai", "DEBIT"),
            makeDemo("USR005", "ACC-9988-****-1122", 120.00, "CVS Pharmacy", "PHARMACY", "US", "Boston", "DEBIT"),
            makeDemo("USR001", "ACC-4521-****-7890", 200.00, "Shell Gas Station", "UTILITIES", "GB", "London", "DEBIT"),
            makeDemo("USR002", "ACC-7834-****-2341", 5000.00, "Mystery Store X", "UNKNOWN", "BR", "Sao Paulo", "TRANSFER")
        );

        for (Transaction txn : demos) {
            analyzeTransaction(txn);
        }
    }

    private Transaction makeDemo(String userId, String account, double amount,
                                  String merchant, String category, String country, String city, String type) {
        Transaction txn = new Transaction(userId, account, amount, merchant, category, country, city, type);
        // Assign random timestamp in past 24h
        txn.setTimestamp(LocalDateTime.now().minusHours((long)(Math.random() * 24)));
        return txn;
    }
}
