# Project Overview

## 1. Problem Statement (`OverviewProblemStatement`)

- **Problem Title**: Modern Banking System Requirements
- **Problem Description**:
  - Traditional banking systems are monolithic and difficult to scale
  - Lack of modularity makes it hard to add new features or extract microservices
  - No unified API documentation for developers
  - Limited observability and monitoring capabilities

- **Problem List**:
  - Monolithic architecture with tight coupling between domains
  - No standardized API documentation (Swagger/OpenAPI)
  - Limited production-ready infrastructure (PostgreSQL, Redis, Kafka need configuration)
  - Missing circuit breaker patterns for external service resilience

---

## 2. Solution (`OverviewSolution`)

- **Solution Title**: Modular Monolith Banking API
- **Solution List** (array of `Solution`):
  - **Solution 1** - Modular Monolith Architecture
    - **Title**: Hexagonal/Ports & Adapters Architecture
    - **Description**: Each domain (IAM, Accounts, Payments, Loans, Notifications, Audit) is a separate Maven module with clear boundaries. Communication only via ApplicationEvents and shared value objects.
  - **Solution 2** - Production-Ready Infrastructure
    - **Title**: Docker Compose with PostgreSQL, Redis, Kafka
    - **Description**: Ready-to-use infrastructure configuration for local development and production deployment with the `docker` profile.

---

## 3. Key Metrics (`OverviewKeyMetrics`)

- **Metrics Title**: Project Health Indicators
- **Metrics List** (strings):
  - 8 Maven modules (bank-shared, bank-iam, bank-accounts, bank-audit, bank-payments, bank-loans, bank-notifications, bank-boot)
  - 12 integration tests passing
  - Java 21 + Spring Boot 4.0.5
  - 7 domain modules with DDD patterns

See also [ProjectMetric.md](ProjectMetric.md) for richer metrics.

---

## 4. Cover Image (`ProjectCoverImage`, optional)

- **URL**: "" <!-- [PLACEHOLDER: Add cover image URL] -->
- **Alt**: "Bank API Architecture Diagram"
- **Credit** (optional): ""

---

## 5. Links (`ProjectLinks`)

See [ProjectLinks.md](ProjectLinks.md).

- **GitHub**: https://github.com/alexistrejo11/bank-api
- **Demo**: "" <!-- [PLACEHOLDER: Add demo URL when available] -->
- **Documentation**: https://github.com/alexistrejo11/bank-api/tree/main/docs
- **Docker Hub**: "" <!-- [PLACEHOLDER: Add Docker Hub URL if published] -->

---

## 6. Media Gallery Section (`MediaGallerySection`)

See [MediaGallerySection.md](MediaGallerySection.md).

- **Title**: Project Screenshots and Diagrams
- **Description**: Visual documentation of the Bank API system
- **Items**: list of media items (see `ProjectMediaItem` in MediaGallerySection.md).

---

## 7. Media Items (`ProjectMediaItem[]`)

For each media item:

- **Type**: `image` | `video`
- **URL**: "" <!-- [PLACEHOLDER: Add media URL] -->
- **Thumbnail** (optional): "" <!-- [PLACEHOLDER: Add thumbnail URL] -->
- **Title**: "" <!-- [PLACEHOLDER: Add title] -->
- **Description**: "" <!-- [PLACEHOLDER: Add description] -->
- **Alt** (optional): "" <!-- [PLACEHOLDER: Add alt text] -->
- **Category** (optional): `screenshot` | `diagram` | `demo` | `architecture`

---

## 8. Metrics (`ProjectMetric[]`)

For each metric see [ProjectMetric.md](ProjectMetric.md):

- **Label**: "Modules"
- **Value**: "8"
- **Description**: "Maven modules in the project"
- **Icon**: "📦"
- **Unit**: "count"
- **Trend**: `stable`

- **Label**: "Tests"
- **Value**: "12"
- **Description**: "Integration tests passing"
- **Icon**: "✅"
- **Unit**: "count"
- **Trend**: `stable`

- **Label**: "Java Version"
- **Value**: "21"
- **Description**: "Java LTS version"
- **Icon**: "☕"
- **Unit**: "version"
- **Trend**: `stable`

- **Label**: "Spring Boot"
- **Value**: "4.0.5"
- **Description**: "Spring Boot framework version"
- **Icon**: "🌱"
- **Unit**: "version"
- **Trend**: `stable`
