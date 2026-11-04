package com.frauddetect.controller;

import com.frauddetect.model.FraudAlert;
import com.frauddetect.model.Transaction;
import com.frauddetect.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for the Fraud Detection System
 *
 * Exposes endpoints for:
 * - Transaction analysis
 * - Transaction listing
 * - Fraud alert management
 * - Dashboard statistics
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSACTION ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/transactions/analyze
     * Submit a transaction for ML-based fraud analysis
     */
    @PostMapping("/transactions/analyze")
    public ResponseEntity<Transaction> analyzeTransaction(@RequestBody Transaction transaction) {
        if (transaction.getUserId() == null || transaction.getUserId().isBlank()) {
            transaction.setUserId("USR" + String.format("%03d", (int)(Math.random() * 999 + 1)));
        }
        if (transaction.getAccountNumber() == null || transaction.getAccountNumber().isBlank()) {
            transaction.setAccountNumber("ACC-" + (int)(Math.random() * 9000 + 1000) + "-****-" + (int)(Math.random() * 9000 + 1000));
        }

        Transaction result = transactionService.analyzeTransaction(transaction);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/transactions
     * Retrieve all analyzed transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ALERT ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/alerts
     * Retrieve all fraud alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<FraudAlert>> getAllAlerts() {
        return ResponseEntity.ok(transactionService.getAllAlerts());
    }

    /**
     * GET /api/alerts/active
     * Retrieve only unresolved alerts
     */
    @GetMapping("/alerts/active")
    public ResponseEntity<List<FraudAlert>> getActiveAlerts() {
        return ResponseEntity.ok(transactionService.getActiveAlerts());
    }

    /**
     * PUT /api/alerts/{alertId}/resolve
     * Resolve a specific fraud alert
     */
    @PutMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable String alertId,
                                           @RequestBody Map<String, String> body) {
        String resolution = body.getOrDefault("resolution", "Manually reviewed and resolved");
        Optional<FraudAlert> alert = transactionService.resolveAlert(alertId, resolution);

        if (alert.isPresent()) {
            return ResponseEntity.ok(alert.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/dashboard/stats
     * Aggregated statistics for the dashboard
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(transactionService.getDashboardStats());
    }

    /**
     * GET /api/health
     * System health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "system", "Fraud Detection System",
            "version", "1.0.0",
            "technology", "Java + ML + REST APIs",
            "domain", "Finance & Security"
        ));
    }
}
