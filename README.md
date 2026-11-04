# 🛡️ FraudShield — AI-Powered Fraud Detection System

> **Domain:** Finance & Security  
> **Technology:** Java · Spring Boot · Machine Learning · REST APIs  
> **Objective:** Enhance financial security by identifying fraudulent transaction patterns in real-time

---

## 📋 Project Details

| Field | Details |
|-------|---------|
| **Project Name** | Fraud Detection System |
| **Domain** | Finance & Security |
| **Description** | A Java-based application using machine learning algorithms to detect suspicious activities in transactions and prevent fraud |
| **Software Requirements** | Java IDE, ML Libraries (Weka/TensorFlow simulation via rule-based engine) |
| **Technology Stack** | Java 17, Spring Boot 3.2, Machine Learning (ensemble scoring), REST APIs |
| **Objective** | Enhance financial security by identifying fraudulent patterns in real-time |
| **Targeted Outcome** | A fraud detection system capable of analyzing transactions and providing alerts for suspicious activity |

---

## 🏗️ Architecture

```
fraud-detection-system/
├── backend/                          ← Java Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/frauddetect/
│       ├── FraudDetectionApplication.java   ← App entry point
│       ├── engine/
│       │   └── MLFraudEngine.java           ← Core ML fraud scoring engine
│       ├── model/
│       │   ├── Transaction.java             ← Transaction data model
│       │   └── FraudAlert.java              ← Alert data model
│       ├── service/
│       │   └── TransactionService.java      ← Business logic + storage
│       └── controller/
│           └── TransactionController.java   ← REST API endpoints
└── frontend/                         ← Web Dashboard
    ├── index.html                    ← Main dashboard UI
    ├── css/style.css                 ← Premium dark glassmorphism styles
    └── js/app.js                     ← Dashboard logic + API integration
```

---

## 🧠 ML Fraud Detection Engine

The `MLFraudEngine` implements a **weighted ensemble scoring system** (0–100):

| Rule | Weight | Detection |
|------|--------|-----------|
| Amount Anomaly | 25% | Z-score deviation from user baseline |
| Merchant Risk | 20% | High-risk category classification |
| Geo-Location Risk | 20% | Country-level fraud probability |
| Velocity Check | 20% | Rapid successive transactions |
| Time-of-Day | 10% | Suspicious hours (1–4 AM) |
| Round Amount Pattern | 5% | Money structuring detection |

**Risk Levels:**
- 🟢 **LOW** (0–34): Transaction approved
- 🟡 **MEDIUM** (35–54): Monitor — minor anomalies
- 🟠 **HIGH** (55–74): Alert — significant risk
- 🔴 **CRITICAL** (75–100): Block — immediate investigation

---

## 🚀 How to Run

### Prerequisites
- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Any modern browser

### Step 1 — Start Backend
```bash
cd backend
mvn spring-boot:run
```
Backend starts at: **http://localhost:8080**

### Step 2 — Open Frontend
Simply open `frontend/index.html` in your browser.

> **Note:** The frontend also works **offline** with simulated data if the backend is not running.

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/transactions/analyze` | Analyze a transaction for fraud |
| `GET` | `/api/transactions` | Get all analyzed transactions |
| `GET` | `/api/alerts` | Get all fraud alerts |
| `GET` | `/api/alerts/active` | Get unresolved alerts only |
| `PUT` | `/api/alerts/{id}/resolve` | Resolve a fraud alert |
| `GET` | `/api/dashboard/stats` | Dashboard aggregate statistics |
| `GET` | `/api/health` | System health check |

### Sample API Call
```bash
curl -X POST http://localhost:8080/api/transactions/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9500.00,
    "merchantName": "CryptoExchangePro",
    "merchantCategory": "CRYPTOCURRENCY",
    "country": "NG",
    "transactionType": "TRANSFER"
  }'
```

---

## 🎨 Dashboard Features

- **📊 Dashboard** — Live stat cards, fraud score trend chart, risk distribution donut chart, recent transaction feed
- **🔍 Analyze Transaction** — Submit transactions to the ML engine with instant fraud score + detailed rule breakdown
- **📋 Transactions** — Full transaction history with risk-level filtering
- **🚨 Fraud Alerts** — Active alerts with severity, description, and resolve button

---

## 🛠️ Technology Details

- **Backend:** Java 17 + Spring Boot 3.2 + Spring Web (REST)
- **ML Engine:** Custom rule-based ensemble scoring (simulates Weka/TensorFlow output)
- **Frontend:** Vanilla HTML/CSS/JS + Chart.js
- **Design:** Dark glassmorphism, Inter + JetBrains Mono fonts, micro-animations
- **Storage:** In-memory (CopyOnWriteArrayList) — swap with DB for production
# Fraud-Detection-System
# Fraud-Detection-System
