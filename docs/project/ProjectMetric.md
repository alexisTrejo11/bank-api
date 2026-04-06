# Project Metrics

## Metrics Summary

### Module Count
- **Label**: "Modules"
- **Value**: "8"
- **Unit**: "count"
- **Description**: "Maven modules in the project (bank-shared, bank-iam, bank-accounts, bank-audit, bank-payments, bank-loans, bank-notifications, bank-boot)"
- **Icon**: "📦"
- **Trend**: `stable`

---

### Test Coverage
- **Label**: "Integration Tests"
- **Value**: "12"
- **Unit**: "tests"
- **Description**: "Integration tests passing (IT suffix)"
- **Icon**: "✅"
- **Trend**: `stable`

---

### Java Version
- **Label**: "Java Version"
- **Value**: "21"
- **Unit**: "version"
- **Description**: "Java LTS (Long Term Support) version"
- **Icon**: "☕"
- **Trend**: `stable`

---

### Spring Boot Version
- **Label**: "Spring Boot"
- **Value**: "4.0.5"
- **Unit**: "version"
- **Description**: "Spring Boot framework version"
- **Icon**: "🌱"
- **Trend**: `stable`

---

### API Endpoints
- **Label**: "API Endpoints"
- **Value**: "16+"
- **Unit**: "count"
- **Description**: "REST API endpoints across all modules"
- **Icon**: "🌐"
- **Trend**: `stable`

---

### Docker Services
- **Label**: "Docker Services"
- **Value**: "7"
- **Unit**: "services"
- **Description**: "Services in docker-compose.yml (postgres, redis, kafka, prometheus, grafana, elasticsearch, kibana)"
- **Icon**: "🐳"
- **Trend**: `stable`

---

### Domain Events
- **Label**: "Domain Events"
- **Value**: "6"
- **Unit**: "events"
- **Description**: "Cross-module domain events (TransferCompletedEvent, TransferFailedEvent, TransferReversedEvent, LoanDisbursedEvent, LoanApprovedEvent, LoanRepaymentCompletedEvent, LoanPaidOffEvent)"
- **Icon**: "📡"
- **Trend**: `stable`

---

### JWT Configuration
- **Label**: "JWT Access Token Expiry"
- **Value**: "15 minutes"
- **Unit**: "time"
- **Description**: "Access token lifetime"
- **Icon**: "🔐"
- **Trend**: `stable`

- **Label**: "JWT Refresh Token Expiry"
- **Value**: "7 days"
- **Unit**: "time"
- **Description**: "Refresh token lifetime in Redis"
- **Icon**: "🔐"
- **Trend**: `stable`

---

### Rate Limiting
- **Label**: "Global Rate Limit"
- **Value**: "64 req/sec"
- **Unit**: "requests/second"
- **Description**: "Per-IP global rate limit"
- **Icon**: "🚦"
- **Trend**: `stable`

- **Label**: "Strict Rate Limit"
- **Value**: "12 req/min"
- **Unit**: "requests/minute"
- **Description**: "Per-user strict profile limit"
- **Icon**: "🚦"
- **Trend**: `stable`

- **Label**: "Sensitive Operations Rate Limit"
- **Value**: "6 req/min"
- **Unit**: "requests/minute"
- **Description**: "Rate limit for sensitive endpoints (transfers, loan repayments)"
- **Icon**: "🚦"
- **Trend**: `stable`

---

### Database
- **Label**: "Database Connections (Pool)"
- **Value**: "10"
- **Unit**: "connections"
- **Description**: "HikariCP default connection pool size"
- **Icon**: "🔗"
- **Trend**: `stable`

- **Label**: "Database Type"
- **Value**: "PostgreSQL / H2"
- **Unit**: "type"
- **Description**: "PostgreSQL for production, H2 in-memory for development"
- **Icon**: "🐘"
- **Trend**: `stable`

---

### Monitoring
- **Label**: "Actuator Endpoints"
- **Value**: "3+"
- **Unit**: "endpoints"
- **Description**: "Health, info, prometheus endpoints"
- **Icon**: "📊"
- **Trend**: `stable`

---

### Code Quality
- **Label**: "Test Pass Rate"
- **Value**: "100%"
- **Unit**: "percentage"
- **Description**: "All integration tests passing"
- **Icon**: "✅"
- **Trend**: `stable`

---

### Build
- **Label**: "Build Time"
- **Value**: "~40 seconds"
- **Unit**: "time"
- **Description**: "Full Maven build + test time"
- **Icon**: "⏱️"
- **Trend**: `stable`
