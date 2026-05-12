# Bank API

A production-ready modular monolith banking API built with Java 21, Spring Boot 4, and Hexagonal Architecture.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/Status-Active-success)

## Overview

Bank API is a comprehensive banking system implementing:
- **Identity & Access Management** — JWT authentication with RS256, refresh tokens, RBAC
- **Accounts** — Double-entry bookkeeping with immutable ledger
- **Payments** — Idempotent transfers between accounts
- **Loans** — Loan origination with amortization schedules
- **Notifications** — Email/SMS async dispatch
- **Audit** — Immutable event log for compliance

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (for full stack)

### Build & Run (Development)

```bash
# Clone and build
git clone https://github.com/alexistrejo11/bank-api.git
cd bank-api

# Run with H2 in-memory database (default)
./mvnw clean verify
./mvnw -pl bank-boot spring-boot:run
```

### Run with PostgreSQL + Redis + Kafka

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Set required values in .env (POSTGRES_PASSWORD, etc.)

# 3. Start infrastructure
docker compose up -d postgres redis kafka

# 4. Run with docker profile
./mvnw -pl bank-boot spring-boot:run -Dspring-profiles.active=docker
```

### Access Points

| Service | URL | Credentials |
|---------|-----|--------------|
| API | http://localhost:8080 | — |
| Swagger UI | http://localhost:8080/swagger-ui.html | JWT (Authorize button) |
| API Docs (JSON) | http://localhost:8080/api-docs | — |
| Actuator Health | http://localhost:8080/actuator/health | — |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus | — |

## Project Structure

```
bank-api/
├── bank-shared/           # Shared kernel (value objects, events, Result<T>)
├── bank-iam/              # Identity & Access Management
├── bank-accounts/         # Accounts & Ledger
├── bank-payments/        # Transfers & Idempotency
├── bank-loans/           # Loans & Amortization
├── bank-notifications/   # Email & SMS dispatch
├── bank-audit/           # Immutable audit records
├── bank-config/          # Cross-cutting security & shared web config
├── bank-boot/            # Spring Boot application
├── docs/                 # Documentation
│   ├── project/          # Project documentation
│   └── v0.2.0/          # Configuration guides
├── docker-compose.yml    # Full stack
└── pom.xml               # Parent POM
```

## Architecture

### Hexagonal Architecture (Ports & Adapters)

Each module follows identical package structure:

```
{module}/
├── presentation/   → REST controllers, DTOs, mappers (some modules may still say api/ in older docs)
├── application/   → Command/Query handlers
├── domain/          → Entities, value objects, ports
└── infrastructure/→ JPA repositories, event listeners, adapters
```

### Key Patterns

| Pattern | Implementation |
|---------|----------------|
| Double-Entry Bookkeeping | Every transfer creates two LedgerEntry rows (debit + credit) |
| Event-Driven | ApplicationEvents trigger cross-module side-effects |
| Idempotency | UUID keys cached in Redis with 24h TTL |
| Rate Limiting | Token bucket algorithm (global + per-user profiles) |
| Result<T> | Explicit error handling with sealed Result interface |

## API Endpoints

All versioned REST routes live under **`/api/v1`**. Full reference (methods, bodies, headers such as **`Idempotency-Key`**, authorities, and response patterns) is maintained in **[docs/project/APISchema.md](docs/project/APISchema.md)**.

| Area | Examples |
|------|-----------|
| Auth | `POST .../auth/register`, `login`, `refresh`, `logout`, `GET .../auth/me` |
| Accounts | `POST .../accounts`, `GET .../accounts/{id}/balance`, `.../ledger` |
| Payments | `POST .../payments/transfers`, `POST .../payments/transfers/{id}/reverse` |
| Loans | `POST .../loans`, `POST .../loans/{id}/approve`, `GET .../loans/{id}`, pay repayment |
| Audit | `GET .../audit/records` |
| Notifications | `GET .../notifications/monitoring/records`, `.../summary` |

## Documentation

### Project Documentation (`docs/project/`)

Start here: **[docs/project/README.md](docs/project/README.md)** — hub page linking all human-readable docs. Machine-readable YAML for tooling lives in **[docs/project/source/](docs/project/source/)** (schema: [`TypescriptSchema.md`](docs/project/source/TypescriptSchema.md)).

| Document | Description |
|----------|-------------|
| [README.md](docs/project/README.md) | Documentation hub and conventions |
| [ProjectMetadata.md](docs/project/ProjectMetadata.md) | Version, status, tech stack, modules |
| [ProjectOverview.md](docs/project/ProjectOverview.md) | Problem statement, solution, metrics |
| [ProjectFeatures.md](docs/project/ProjectFeatures.md) | Nine feature areas with status and highlights |
| [ProjectArchitectureModel.md](docs/project/ProjectArchitectureModel.md) | Layers, patterns, AWS-shaped diagram, data flows, ADRs |
| [InfrastructureModel.md](docs/project/InfrastructureModel.md) | Local, Docker Compose, AWS, Dockerfile |
| [APISchema.md](docs/project/APISchema.md) | REST API reference |
| [ProjectCodeShowCase.md](docs/project/ProjectCodeShowCase.md) | Annotated code excerpts |
| [ProjectMetric.md](docs/project/ProjectMetric.md) | Product and infra metrics |
| [ProjectLinks.md](docs/project/ProjectLinks.md) | Repository, docs tree, demo placeholders |
| [MediaGallerySection.md](docs/project/MediaGallerySection.md) | Screenshot / diagram placeholders |

### Configuration Guides

| Document | Description |
|----------|-------------|
| [docs/v0.2.0/CONFIGURATION.md](docs/v0.2.0/CONFIGURATION.md) | Environment variables |
| [docs/v0.2.0/DATABASE.md](docs/v0.2.0/DATABASE.md) | Database setup |
| [docs/v0.2.0/ROADMAP.md](docs/v0.2.0/ROADMAP.md) | Planned improvements |

## Tech Stack

| Category | Technology |
|----------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Architecture | Hexagonal / Modular Monolith |
| Security | Spring Security 6 + JWT RS256 |
| Database | PostgreSQL 16 / H2 (dev) |
| Cache | Redis 7 |
| Messaging | Kafka (Confluent in Compose; MSK on AWS) |
| API Docs | SpringDoc OpenAPI |
| Metrics | Micrometer + Prometheus |
| Logs | Logback JSON → Elasticsearch → Kibana |
| Testing | JUnit 5 + Testcontainers |
| Build | Maven 3.9 |

## Configuration

### Environment Variables

Key variables (see `.env.example` for full list):

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bank
SPRING_DATASOURCE_USERNAME=bank_user
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# JWT Keys (PEM format)
BANK_SECURITY_JWT_PRIVATE_KEY_PEM=-----BEGIN RSA PRIVATE KEY-----
BANK_SECURITY_JWT_PUBLIC_KEY_PEM=-----BEGIN PUBLIC KEY-----
```

### Profiles

| Profile | Description |
|---------|-------------|
| `default` | H2 in-memory, no Redis/Kafka |
| `test` | H2 + testcontainers |
| `postgres` | PostgreSQL, no Redis/Kafka |
| `docker` | Full stack (PostgreSQL + Redis + Kafka) |

## Docker Compose Services

```bash
# Start all services
docker compose up -d

# Services available:
# - app:8080        (Bank API)
# - postgres:5432   (Database)
# - redis:6379     (Cache)
# - kafka:19092    (Message broker)
# - prometheus:9090 (Metrics)
# - grafana:3000   (Dashboards)
```

## Security

- **JWT**: RS256 asymmetric keys, 15-minute access tokens
- **Refresh Tokens**: 7-day TTL in Redis with rotation
- **RBAC**: Roles → Permissions → Endpoint authorization
- **Rate Limiting**: Global per-IP + per-user profiles
- **Audit**: Immutable append-only records

## Testing

```bash
# Run all tests
./mvnw verify

# Run specific module tests
./mvnw -pl bank-iam test

# Run integration tests only
./mvnw -pl bank-boot verify -DskipTests=false
```

## Future Enhancements

See [docs/v0.2.0/ROADMAP.md](docs/v0.2.0/ROADMAP.md) for planned features:

- AWS cloud deployment (RDS, ElastiCache, MSK)
- Circuit breaker for external services
- Enhanced observability with custom metrics
- API Gateway for centralized auth

## License

MIT License - see [LICENSE](LICENSE) for details.

## Author

**Alexis Trejo**
- GitHub: [@alexistrejo11](https://github.com/alexistrejo11)
- Repository: https://github.com/alexistrejo11/bank-api

---

*For detailed technical documentation, see the `docs/` directory.*
