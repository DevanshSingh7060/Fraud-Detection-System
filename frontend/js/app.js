/**
 * FraudShield — Frontend Application Logic
 * Fraud Detection System | Java + ML + REST APIs
 * 
 * Connects to the Spring Boot backend REST API at http://localhost:8080
 * and drives the full dashboard: charts, transaction feed, alerts, analysis form.
 */

'use strict';

// ─── CONFIGURATION ───────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';
const REFRESH_INTERVAL = 15000; // Auto-refresh every 15 seconds

// ─── GLOBAL STATE ─────────────────────────────────────────────────────────────
let fraudTrendChart = null;
let riskDistChart   = null;
let allTransactions = [];
let autoRefreshTimer = null;
let backendOnline = false;

// ─── INITIALIZATION ───────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    startClock();
    checkBackendStatus();
    showSection('dashboard');
    startAutoRefresh();
});

// ─── CLOCK ────────────────────────────────────────────────────────────────────
function startClock() {
    const el = document.getElementById('current-time');
    const tick = () => {
        const now = new Date();
        el.textContent = now.toLocaleTimeString('en-US', { hour12: false });
    };
    tick();
    setInterval(tick, 1000);
}

// ─── BACKEND STATUS CHECK ─────────────────────────────────────────────────────
async function checkBackendStatus() {
    try {
        const res = await fetch(`${API_BASE}/health`, { signal: AbortSignal.timeout(3000) });
        if (res.ok) {
            backendOnline = true;
            showToast('success', '✅ Connected to ML Engine (port 8080)');
            loadDashboard();
            loadTransactions();
            loadAlerts();
        } else {
            handleBackendOffline();
        }
    } catch {
        handleBackendOffline();
    }
}

function handleBackendOffline() {
    backendOnline = false;
    showToast('warning', '⚠️ Backend offline — showing mock data');
    loadMockDashboard();
    loadMockTransactions();
    loadMockAlerts();
}

// ─── SECTION NAVIGATION ───────────────────────────────────────────────────────
function showSection(name) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

    // Show target
    const section = document.getElementById(`section-${name}`);
    const navItem = document.getElementById(`nav-${name}`);
    if (section) section.classList.add('active');
    if (navItem) navItem.classList.add('active');

    // Update header
    const titles = {
        dashboard:    ['Dashboard Overview',        'Real-time ML-powered fraud detection and transaction analysis'],
        analyze:      ['Analyze Transaction',       'Submit a transaction to the ML fraud detection engine'],
        transactions: ['Transaction History',       'All transactions analyzed by the machine learning engine'],
        alerts:       ['Fraud Alerts',              'Active fraud alerts and suspicious activity notifications']
    };
    if (titles[name]) {
        document.getElementById('page-title').textContent    = titles[name][0];
        document.getElementById('page-subtitle').textContent = titles[name][1];
    }

    // Lazy-load section data
    if (name === 'transactions' && allTransactions.length === 0) loadTransactions();
    if (name === 'alerts') loadAlerts();

    return false;
}

// ─── AUTO REFRESH ─────────────────────────────────────────────────────────────
function startAutoRefresh() {
    autoRefreshTimer = setInterval(() => {
        if (backendOnline) {
            loadDashboard();
        }
    }, REFRESH_INTERVAL);
}

// ═══════════════════════════════════════════════════════════════════════════════
// DASHBOARD
// ═══════════════════════════════════════════════════════════════════════════════

async function loadDashboard() {
    try {
        const res = await fetch(`${API_BASE}/dashboard/stats`);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const stats = await res.json();
        renderDashboardStats(stats);
        renderFraudTrendChart(stats.recentScores || []);
        renderRiskDistributionChart(stats.riskDistribution || {});
        renderRecentTransactions(stats.recentScores || []);
    } catch (err) {
        console.warn('Dashboard load failed:', err);
        if (!backendOnline) loadMockDashboard();
    }
}

function renderDashboardStats(stats) {
    animateCounter('stat-total',   stats.totalTransactions  || 0);
    animateCounter('stat-flagged', stats.flaggedTransactions || 0);
    setText('stat-blocked',  '$' + formatAmount(stats.blockedAmount || 0));
    animateCounter('stat-alerts',  stats.activeAlerts       || 0);
    setText('stat-fraud-rate', (stats.fraudRate || 0).toFixed(1) + '% fraud rate');

    // Update alert badge in sidebar
    const badge = document.getElementById('alert-badge');
    const activeAlerts = stats.activeAlerts || 0;
    badge.textContent = activeAlerts;
    badge.style.display = activeAlerts > 0 ? 'flex' : 'none';

    // Legend
    const dist = stats.riskDistribution || {};
    setText('leg-critical', dist.CRITICAL || 0);
    setText('leg-high',     dist.HIGH     || 0);
    setText('leg-medium',   dist.MEDIUM   || 0);
    setText('leg-low',      dist.LOW      || 0);
    setText('donut-total',  stats.totalTransactions || 0);
}

// ─── CHARTS ───────────────────────────────────────────────────────────────────

function renderFraudTrendChart(scores) {
    const ctx = document.getElementById('fraudTrendChart').getContext('2d');
    const labels  = scores.map((_, i) => `T-${scores.length - i}`).reverse();
    const data    = [...scores].reverse().map(s => s.score || s);
    const colors  = data.map(v => scoreColor(v));

    if (fraudTrendChart) fraudTrendChart.destroy();

    fraudTrendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: 'Fraud Score',
                data,
                borderColor: '#6366f1',
                backgroundColor: (ctx) => {
                    const gradient = ctx.chart.ctx.createLinearGradient(0, 0, 0, 220);
                    gradient.addColorStop(0,   'rgba(99,102,241,0.3)');
                    gradient.addColorStop(1,   'rgba(99,102,241,0)');
                    return gradient;
                },
                borderWidth: 2.5,
                pointBackgroundColor: colors,
                pointBorderColor: colors,
                pointRadius: 4,
                pointHoverRadius: 6,
                fill: true,
                tension: 0.4,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(8,11,22,0.95)',
                    borderColor: 'rgba(99,102,241,0.3)',
                    borderWidth: 1,
                    titleColor: '#a5b4fc',
                    bodyColor: '#94a3b8',
                    padding: 12,
                    callbacks: {
                        label: ctx => ` Fraud Score: ${ctx.parsed.y.toFixed(1)}`
                    }
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255,255,255,0.04)', drawBorder: false },
                    ticks: { color: '#475569', font: { size: 10 } }
                },
                y: {
                    min: 0, max: 100,
                    grid: { color: 'rgba(255,255,255,0.04)', drawBorder: false },
                    ticks: {
                        color: '#475569',
                        font: { size: 10 },
                        callback: v => v + '%'
                    }
                }
            }
        }
    });
}

function renderRiskDistributionChart(dist) {
    const ctx = document.getElementById('riskDistributionChart').getContext('2d');
    const data = [dist.CRITICAL || 0, dist.HIGH || 0, dist.MEDIUM || 0, dist.LOW || 0];

    if (riskDistChart) riskDistChart.destroy();

    riskDistChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Critical', 'High', 'Medium', 'Low'],
            datasets: [{
                data,
                backgroundColor: [
                    'rgba(239,68,68,0.8)',
                    'rgba(249,115,22,0.8)',
                    'rgba(234,179,8,0.8)',
                    'rgba(34,197,94,0.8)'
                ],
                borderColor: [
                    '#ef4444', '#f97316', '#eab308', '#22c55e'
                ],
                borderWidth: 2,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '72%',
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(8,11,22,0.95)',
                    borderColor: 'rgba(99,102,241,0.3)',
                    borderWidth: 1,
                    titleColor: '#a5b4fc',
                    bodyColor: '#94a3b8',
                    padding: 12
                }
            }
        }
    });
}

function renderRecentTransactions(scores) {
    const tbody = document.getElementById('txn-tbody');
    if (!scores || scores.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="loading-row">No transactions found</td></tr>';
        return;
    }

    tbody.innerHTML = scores.map(t => `
        <tr class="row-${(t.risk || 'LOW').toLowerCase()}">
            <td><span class="txn-id">${t.id || '—'}</span></td>
            <td>${t.userId || '—'}</td>
            <td class="amount-cell">$${formatAmount(t.amount || 0)}</td>
            <td>${t.merchantName || '—'}</td>
            <td>${t.country || '—'}</td>
            <td>${renderScoreBar(t.score || 0)}</td>
            <td>${renderRiskBadge(t.risk || 'LOW')}</td>
            <td>${(t.score || 0) >= 35 ? '<span class="flag-yes">🚨 Flagged</span>' : '<span class="flag-no">✅ Clear</span>'}</td>
        </tr>
    `).join('');
}

// ═══════════════════════════════════════════════════════════════════════════════
// TRANSACTIONS PAGE
// ═══════════════════════════════════════════════════════════════════════════════

async function loadTransactions() {
    const tbody = document.getElementById('all-txn-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="9" class="loading-row"><div class="loader"></div> Loading...</td></tr>';

    try {
        const res = await fetch(`${API_BASE}/transactions`);
        if (!res.ok) throw new Error();
        allTransactions = await res.json();
        renderAllTransactions(allTransactions);
    } catch {
        if (!backendOnline) renderMockTransactions();
    }
}

function filterTransactions() {
    const risk = document.getElementById('filter-risk').value;
    const filtered = risk ? allTransactions.filter(t => t.riskLevel === risk) : allTransactions;
    renderAllTransactions(filtered);
}

function renderAllTransactions(txns) {
    const tbody = document.getElementById('all-txn-tbody');
    if (!txns || txns.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="loading-row">No transactions</td></tr>';
        return;
    }

    tbody.innerHTML = txns.map(t => `
        <tr class="row-${(t.riskLevel || 'LOW').toLowerCase()}">
            <td><span class="txn-id">${t.transactionId || '—'}</span></td>
            <td>${t.userId || '—'}</td>
            <td class="amount-cell">$${formatAmount(t.amount || 0)} <small style="color:var(--text-muted)">${t.currency || 'USD'}</small></td>
            <td>${t.merchantName || '—'}</td>
            <td><span style="font-size:11px;color:var(--text-muted)">${t.merchantCategory || '—'}</span></td>
            <td>${t.country || '—'}</td>
            <td>${renderScoreBar(t.fraudScore || 0)}</td>
            <td>${renderRiskBadge(t.riskLevel || 'LOW')}</td>
            <td>${t.flagged ? '<span class="flag-yes">🚨</span>' : '<span class="flag-no">✅</span>'}</td>
        </tr>
    `).join('');
}

// ═══════════════════════════════════════════════════════════════════════════════
// ALERTS PAGE
// ═══════════════════════════════════════════════════════════════════════════════

async function loadAlerts() {
    const container = document.getElementById('alerts-container');
    if (!container) return;

    container.innerHTML = '<div class="loading-row"><div class="loader"></div> Loading alerts...</div>';

    try {
        const res = await fetch(`${API_BASE}/alerts`);
        if (!res.ok) throw new Error();
        const alerts = await res.json();
        renderAlerts(alerts, container);
    } catch {
        if (!backendOnline) renderMockAlerts(container);
    }
}

function renderAlerts(alerts, container) {
    if (!alerts || alerts.length === 0) {
        container.innerHTML = '<div class="loading-row" style="color:var(--risk-low)">✅ No active fraud alerts</div>';
        return;
    }

    const icons = { CRITICAL: '🚨', HIGH: '⚠️', MEDIUM: '🔔', LOW: '📋' };
    const typeLabels = {
        VELOCITY:         '⚡ Velocity Check',
        AMOUNT_ANOMALY:   '💰 Amount Anomaly',
        GEO_MISMATCH:     '🌍 Geo-Location Mismatch',
        HIGH_RISK_MERCHANT:'🏪 High-Risk Merchant',
        TIME_ANOMALY:     '🕐 Time-of-Day Anomaly',
        BEHAVIORAL_ANOMALY:'🧠 Behavioral Anomaly',
        GENERAL_FRAUD:    '🔴 General Fraud'
    };

    container.innerHTML = `<div class="alerts-container">
        ${alerts.map(a => `
        <div class="alert-card severity-${a.severity || 'LOW'} ${a.resolved ? 'resolved' : ''}" id="alert-${a.alertId}">
            <div class="alert-icon">${icons[a.severity] || '🔔'}</div>
            <div class="alert-body">
                <div class="alert-title">${typeLabels[a.alertType] || a.alertType} — Score: ${(a.fraudScore || 0).toFixed(1)}</div>
                <div class="alert-description">${a.description || 'Suspicious activity detected'}</div>
                <div class="alert-meta">
                    <span>ID: ${a.alertId}</span>
                    <span>TXN: ${a.transactionId}</span>
                    <span>Acc: ${a.accountNumber || '—'}</span>
                    <span>Amount: $${formatAmount(a.transactionAmount || 0)}</span>
                    ${a.resolved ? '<span style="color:var(--risk-low)">✓ Resolved</span>' : ''}
                </div>
            </div>
            <div class="alert-actions">
                ${renderRiskBadge(a.severity || 'LOW')}
                ${!a.resolved ? `<button class="btn-resolve" onclick="resolveAlert('${a.alertId}')">Resolve</button>` : ''}
            </div>
        </div>
        `).join('')}
    </div>`;
}

async function resolveAlert(alertId) {
    try {
        const res = await fetch(`${API_BASE}/alerts/${alertId}/resolve`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ resolution: 'Manually reviewed and cleared by analyst' })
        });
        if (res.ok) {
            showToast('success', '✅ Alert resolved successfully');
            loadAlerts();
            loadDashboard();
        } else {
            throw new Error();
        }
    } catch {
        // Optimistic UI update for offline mode
        const card = document.getElementById(`alert-${alertId}`);
        if (card) {
            card.classList.add('resolved');
            card.querySelector('.btn-resolve')?.remove();
            showToast('success', '✅ Alert marked as resolved');
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TRANSACTION ANALYSIS FORM
// ═══════════════════════════════════════════════════════════════════════════════

async function analyzeTransaction(e) {
    e.preventDefault();

    const btn     = document.getElementById('analyze-btn');
    const form    = document.getElementById('analyze-form');
    const btnText = btn.querySelector('.btn-text');
    const btnLoad = btn.querySelector('.btn-loading');

    // Loading state
    btn.disabled = true;
    btnText.classList.add('hidden');
    btnLoad.classList.remove('hidden');

    const payload = {
        amount:           parseFloat(document.getElementById('f-amount').value),
        currency:         document.getElementById('f-currency').value,
        userId:           document.getElementById('f-userId').value || null,
        merchantName:     document.getElementById('f-merchant').value,
        merchantCategory: document.getElementById('f-category').value,
        transactionType:  document.getElementById('f-type').value,
        country:          document.getElementById('f-country').value,
        city:             document.getElementById('f-city').value || null,
    };

    try {
        let result;
        if (backendOnline) {
            const res = await fetch(`${API_BASE}/transactions/analyze`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('Analysis failed');
            result = await res.json();
        } else {
            // Offline simulation
            result = simulateFraudAnalysis(payload);
        }

        renderAnalysisResult(result);
        showToast(
            result.riskLevel === 'CRITICAL' || result.riskLevel === 'HIGH' ? 'error' : 'success',
            `Analysis complete — Risk: ${result.riskLevel} (Score: ${result.fraudScore.toFixed(1)})`
        );

        // Refresh dashboard if on dashboard
        if (backendOnline) {
            setTimeout(() => { loadDashboard(); loadTransactions(); }, 500);
        }

    } catch (err) {
        showToast('error', '❌ Analysis failed: ' + err.message);
        console.error(err);
    } finally {
        btn.disabled = false;
        btnText.classList.remove('hidden');
        btnLoad.classList.add('hidden');
    }
}

function renderAnalysisResult(data) {
    const placeholder = document.getElementById('result-placeholder');
    const content     = document.getElementById('result-content');

    placeholder.classList.add('hidden');
    content.classList.remove('hidden');

    const score    = data.fraudScore || 0;
    const risk     = data.riskLevel  || 'LOW';
    const rules    = data.triggeredRules || [];
    const bgClass  = `bg-${risk.toLowerCase()}`;
    const scoreColor = getScoreColor(score);

    // SVG ring calculation
    const circumference = 2 * Math.PI * 30; // r=30
    const offset        = circumference - (score / 100) * circumference;

    content.innerHTML = `
        <div class="result-risk-header ${bgClass}">
            <div class="result-score-ring">
                <svg viewBox="0 0 72 72">
                    <circle class="ring-bg"   cx="36" cy="36" r="30"/>
                    <circle class="ring-fill" cx="36" cy="36" r="30"
                        stroke="${scoreColor}"
                        stroke-dasharray="${circumference}"
                        stroke-dashoffset="${offset}"/>
                </svg>
                <div class="result-score-text">
                    <span class="score-num" style="color:${scoreColor}">${score.toFixed(0)}</span>
                    <small>/ 100</small>
                </div>
            </div>
            <div class="result-verdict">
                <h2 style="color:${scoreColor}">${riskVerdict(risk)}</h2>
                ${renderRiskBadge(risk)}
                <p style="margin-top:8px">${data.fraudReason || 'Analysis complete'}</p>
            </div>
        </div>

        <div class="result-details">
            <div class="detail-row">
                <span class="detail-label">Transaction ID</span>
                <span class="detail-value">${data.transactionId || '—'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Amount</span>
                <span class="detail-value">$${formatAmount(data.amount || 0)} ${data.currency || 'USD'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Merchant</span>
                <span class="detail-value">${data.merchantName || '—'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Category</span>
                <span class="detail-value">${data.merchantCategory || '—'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Country</span>
                <span class="detail-value">${data.country || '—'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Transaction Type</span>
                <span class="detail-value">${data.transactionType || '—'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Fraud Score</span>
                <span class="detail-value" style="color:${scoreColor};font-size:15px;font-weight:800">${score.toFixed(2)} / 100</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Action</span>
                <span class="detail-value">${data.flagged ? '🚨 FLAGGED FOR REVIEW' : '✅ APPROVED'}</span>
            </div>
        </div>

        <div class="triggered-rules">
            <h4>Triggered ML Rules (${rules.length})</h4>
            ${rules.length > 0
                ? rules.map(r => `<div class="rule-tag">${r}</div>`).join('')
                : '<div class="rule-tag safe">No suspicious patterns detected — transaction appears legitimate</div>'
            }
        </div>
    `;
}

// ─── DEMO DATA FILLERS ─────────────────────────────────────────────────────────
function fillDemoData(type) {
    if (type === 'low') {
        document.getElementById('f-amount').value     = '47.50';
        document.getElementById('f-merchant').value  = 'Whole Foods Market';
        document.getElementById('f-category').value  = 'GROCERY';
        document.getElementById('f-country').value   = 'US';
        document.getElementById('f-city').value      = 'Boston';
        document.getElementById('f-type').value      = 'DEBIT';
        document.getElementById('f-userId').value    = 'USR001';
    } else {
        document.getElementById('f-amount').value     = '9500.00';
        document.getElementById('f-merchant').value  = 'CryptoExchangePro';
        document.getElementById('f-category').value  = 'CRYPTOCURRENCY';
        document.getElementById('f-country').value   = 'NG';
        document.getElementById('f-city').value      = 'Lagos';
        document.getElementById('f-type').value      = 'TRANSFER';
        document.getElementById('f-userId').value    = 'USR002';
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// OFFLINE SIMULATION — when backend is not running
// ═══════════════════════════════════════════════════════════════════════════════

function simulateFraudAnalysis(payload) {
    const HIGH_RISK_MERCHANTS = ['CRYPTOCURRENCY','GAMBLING','ADULT_CONTENT','MONEY_TRANSFER','PAWN_SHOP'];
    const HIGH_RISK_COUNTRIES = ['NG','RO','RU','UA','BR'];

    let score = 0;
    const rules = [];

    if (payload.amount > 10000)  { score += 25; rules.push('CRITICAL_AMOUNT: Exceeds $10,000 threshold'); }
    else if (payload.amount > 5000) { score += 15; rules.push('LARGE_AMOUNT: Transaction above normal range'); }
    else if (payload.amount < 1) { score += 15; rules.push('MICRO_TRANSACTION: Probe transaction detected'); }
    else score += (payload.amount / 1000) * 3;

    if (HIGH_RISK_MERCHANTS.includes(payload.merchantCategory)) {
        score += 20; rules.push(`HIGH_RISK_MERCHANT: ${payload.merchantCategory} is a flagged category`);
    } else if (['JEWELRY','ELECTRONICS','ONLINE_GAMING'].includes(payload.merchantCategory)) {
        score += 10; rules.push(`ELEVATED_MERCHANT_RISK: ${payload.merchantCategory} has elevated risk`);
    }

    if (HIGH_RISK_COUNTRIES.includes(payload.country)) {
        score += 20; rules.push(`HIGH_RISK_COUNTRY: ${payload.country} is in elevated fraud risk region`);
    }

    const hour = new Date().getHours();
    if (hour >= 1 && hour <= 4) { score += 10; rules.push(`SUSPICIOUS_HOUR: Transaction at ${hour}:00 AM`); }

    if (payload.amount >= 1000 && payload.amount % 1000 === 0) {
        score += 5; rules.push('ROUND_AMOUNT: Structured transaction pattern');
    }

    score = Math.min(Math.round(score * 10) / 10, 100);
    const risk = score >= 75 ? 'CRITICAL' : score >= 55 ? 'HIGH' : score >= 35 ? 'MEDIUM' : 'LOW';

    return {
        transactionId:    'TXN-' + Math.random().toString(36).substring(2, 10).toUpperCase(),
        userId:           payload.userId || 'USR-SIM',
        amount:           payload.amount,
        currency:         payload.currency || 'USD',
        merchantName:     payload.merchantName,
        merchantCategory: payload.merchantCategory,
        country:          payload.country,
        city:             payload.city,
        transactionType:  payload.transactionType,
        fraudScore:       score,
        riskLevel:        risk,
        flagged:          score >= 35,
        triggeredRules:   rules,
        fraudReason:      rules.length > 0
            ? (score >= 75 ? 'CRITICAL FRAUD RISK: ' : score >= 55 ? 'HIGH FRAUD RISK: ' : 'MEDIUM RISK: ') + rules[0]
            : 'No suspicious patterns detected'
    };
}

function loadMockDashboard() {
    const mockStats = {
        totalTransactions: 12, flaggedTransactions: 7, blockedAmount: 29500,
        activeAlerts: 5, fraudRate: 58.3,
        riskDistribution: { CRITICAL: 3, HIGH: 2, MEDIUM: 2, LOW: 5 },
        recentScores: generateMockScores()
    };
    renderDashboardStats(mockStats);
    renderFraudTrendChart(mockStats.recentScores);
    renderRiskDistributionChart(mockStats.riskDistribution);
    renderRecentTransactions(mockStats.recentScores);
}

function generateMockScores() {
    const risks = ['CRITICAL','HIGH','MEDIUM','LOW'];
    return Array.from({ length: 12 }, (_, i) => {
        const score = Math.round(Math.random() * 100 * 10) / 10;
        return {
            id: 'TXN-' + Math.random().toString(36).substring(2,10).toUpperCase(),
            userId: 'USR00' + (i % 5 + 1),
            score,
            risk: score >= 75 ? 'CRITICAL' : score >= 55 ? 'HIGH' : score >= 35 ? 'MEDIUM' : 'LOW',
            amount: Math.round(Math.random() * 9000 + 50),
            merchantName: ['Amazon','CryptoExchange','Casino Royal','Walmart','MoneyGram'][i % 5],
            country: ['US','NG','RO','GB','RU'][i % 5],
            timestamp: new Date(Date.now() - i * 3600000).toISOString()
        };
    });
}

function loadMockTransactions() {
    allTransactions = generateMockScores().map(s => ({
        transactionId: s.id, userId: s.userId, amount: s.amount, currency: 'USD',
        merchantName: s.merchantName, merchantCategory: ['CRYPTOCURRENCY','GAMBLING','GROCERY','ELECTRONICS','MONEY_TRANSFER'][Math.floor(Math.random()*5)],
        country: s.country, fraudScore: s.score, riskLevel: s.risk, flagged: s.score >= 35
    }));
    renderAllTransactions(allTransactions);
}

function loadMockAlerts() {
    const container = document.getElementById('alerts-container');
    if (!container) return;
    const mockAlerts = generateMockScores()
        .filter(s => s.score >= 35)
        .map(s => ({
            alertId: 'ALT-' + Math.random().toString(36).substring(2,10).toUpperCase(),
            transactionId: s.id,
            alertType: ['VELOCITY','AMOUNT_ANOMALY','GEO_MISMATCH','HIGH_RISK_MERCHANT'][Math.floor(Math.random()*4)],
            severity: s.risk, description: 'Suspicious transaction pattern detected by ML engine',
            fraudScore: s.score, accountNumber: 'ACC-****-' + Math.floor(1000+Math.random()*9000),
            transactionAmount: s.amount, resolved: false
        }));
    renderAlerts(mockAlerts, container);
    document.getElementById('alert-badge').textContent = mockAlerts.length;
}

function renderMockTransactions() {
    loadMockTransactions();
}
function renderMockAlerts(container) {
    loadMockAlerts();
}

// ═══════════════════════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════════════════════

function renderScoreBar(score) {
    const color = getScoreColor(score);
    return `
        <div class="score-cell">
            <span class="score-number" style="color:${color}">${score.toFixed(1)}</span>
            <div class="score-bar">
                <div class="score-fill" style="width:${score}%;background:${color}"></div>
            </div>
        </div>`;
}

function renderRiskBadge(risk) {
    return `<span class="risk-badge badge-${risk}">${risk}</span>`;
}

function getScoreColor(score) {
    if (score >= 75) return '#ef4444';
    if (score >= 55) return '#f97316';
    if (score >= 35) return '#eab308';
    return '#22c55e';
}

function scoreColor(score) { return getScoreColor(score); }

function riskVerdict(risk) {
    const v = { CRITICAL: 'CRITICAL FRAUD DETECTED', HIGH: 'HIGH FRAUD RISK', MEDIUM: 'MEDIUM RISK', LOW: 'TRANSACTION APPROVED' };
    return v[risk] || risk;
}

function formatAmount(n) {
    return Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

function animateCounter(id, target) {
    const el = document.getElementById(id);
    if (!el) return;
    const start = parseInt(el.textContent) || 0;
    const duration = 800;
    const startTime = performance.now();
    const step = (now) => {
        const progress = Math.min((now - startTime) / duration, 1);
        const ease = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(start + (target - start) * ease);
        if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
}

function showToast(type, message) {
    const container = document.getElementById('toast-container');
    const icons = { success: '✅', error: '❌', warning: '⚠️' };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span class="toast-icon">${icons[type] || 'ℹ️'}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(20px)';
        toast.style.transition = '0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}
