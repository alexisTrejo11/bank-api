# Infrastructure Model

## 1. Deployment Layers (`DeploymentLayer[]`)

### Layer 1: Development Local

- **Name**: "Development Local"
- **Color**: "#4CAF50"
- **Components** (`DeploymentComponent[]`):
  - **Component 1**: "H2 In-Memory Database"
    - **Icon**: "🗄️"
    - **Description**: "H2 in PostgreSQL compatibility mode for local development without external dependencies"
  - **Component 2**: "Java 21 (Spring Boot)"
    - **Icon**: "☕"
    - **Description**: "Application running locally via Maven wrapper"

---

### Layer 2: Docker Compose (Local Production Simulation)

- **Name**: "Docker Compose Stack"
- **Color**: "#2196F3"
- **Components** (`DeploymentComponent[]`):
  - **Component 1**: "PostgreSQL"
    - **Icon**: "🐘"
    - **Description**: "Primary database, v16-alpine, port 5432"
  - **Component 2**: "Redis"
    - **Icon**: "💾"
    - **Description**: "Session cache, rate limiting, idempotency, v7-alpine, port 6379"
  - **Component 3**: "Redpanda (Kafka)"
    - **Icon**: "📨"
    - **Description**: "Kafka-compatible message broker, port 19092"
  - **Component 4**: "Prometheus"
    - **Icon**: "📊"
    - **Description**: "Metrics collection, port 9090"
  - **Component 5**: "Grafana"
    - **Icon**: "📈"
    - **Description**: "Dashboards, port 3000"
  - **Component 6**: "Elasticsearch"
    - **Icon**: "🔍"
    - **Description**: "Log storage, port 9200 (future)"
  - **Component 7**: "Kibana"
    - **Icon**: "🔎"
    - **Description**: "Log exploration, port 5601 (future)"

---

### Layer 3: Cloud (Future/AWS)

- **Name**: "Cloud Deployment (Future)"
- **Color**: "#FF9800"
- **Components** (`DeploymentComponent[]`):
  - **Component 1**: "AWS RDS PostgreSQL"
    - **Icon**: "☁️"
    - **Description**: "Managed PostgreSQL, production-grade"
  - **Component 2**: "AWS ElastiCache Redis"
    - **Icon**: "☁️"
    - **Description**: "Managed Redis, production-grade"
  - **Component 3**: "AWS MSK (Kafka)"
    - **Icon**: "☁️"
    - **Description**: "Managed Kafka, production-grade"
  - **Component 4**: "AWS API Gateway"
    - **Icon**: "🚪"
    - **Description**: "Entry point, rate limiting, auth"
  - **Component 5**: "AWS CloudWatch"
    - **Icon**: "📊"
    - **Description**: "Monitoring and logging"
  - **Component 6**: "AWS OpenSearch"
    - **Icon**: "🔍"
    - **Description**: "Log analysis (replaces ELK)"

---

## 2. Docker Files (`DockerFile[]`)

### Service 1: app (Spring Boot Application)

- **Service**: "app"
- **Description**: "Bank API modular monolith application"
- **Content**:
  ```dockerfile
  # Multi-stage build for the Bank API application
  # [PLACEHOLDER: Add Dockerfile content when created]
  ```

---

## 3. Cloud Services (`CloudService[]`)

For each service:

- **Name**: "PostgreSQL (RDS)"
- **Purpose**: "Primary database for all modules"
- **Icon**: "🐘"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "ElastiCache (Redis)"
- **Purpose**: "Token storage, rate limiting, idempotency cache"
- **Icon**: "💾"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "MSK (Kafka)"
- **Purpose**: "Event streaming for notifications"
- **Icon**: "📨"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "API Gateway"
- **Purpose**: "Entry point, auth, rate limiting"
- **Icon**: "🚪"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "CloudWatch"
- **Purpose**: "Metrics and logging"
- **Icon**: "📊"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "OpenSearch"
- **Purpose**: "Log analysis and search"
- **Icon**: "🔍"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "S3"
- **Purpose**: "Static assets, reports storage"
- **Icon**: "📦"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

- **Name**: "Lambda (Future)"
- **Purpose**: "Serverless functions for specific tasks"
- **Icon**: "λ"
- **Cost**: "<!-- [PLACEHOLDER: Add estimated cost] -->"

---

## 4. Metrics (`InfrastructureMetric[]`)

For each metric:

- **Label**: "Docker Services"
- **Value**: "7"
- **Icon**: "🐳"
- **Description**: "Services in docker-compose.yml (postgres, redis, kafka, prometheus, grafana, elasticsearch, kibana)"

- **Label**: "Health Checks"
- **Value**: "3"
- **Icon**: "❤️"
- **Description**: "Liveness, readiness, and db health endpoints"

- **Label**: "Profiles"
- **Value**: "4"
- **Icon**: "⚙️"
- **Description**: "Active profiles: default (H2), test, postgres, docker"

- **Label**: "Database Connections"
- **Value**: "10 (default HikariCP)"
- **Icon**: "🔗"
- **Description**: "HikariCP connection pool size"

- **Label**: "Redis Expiry (JWT)"
- **Value**: "7 days"
- **Icon**: "⏰"
- **Description**: "Refresh token TTL in Redis"

- **Label**: "JWT Expiry"
- **Value**: "15 minutes"
- **Icon**: "🔐"
- **Description**: "Access token lifetime"

- **Label**: "Rate Limit (Global)"
- **Value**: "64 req/sec"
- **Icon**: "🚦"
- **Description**: "Global per-IP rate limit"

- **Label**: "Rate Limit (Strict)"
- **Value**: "12 req/min"
- **Icon**: "🚦"
- **Description**: "Per-user strict profile limit"
