# Architecture Model

## 1. Layers (`ArchitectureLayer[]`)

### Layer 1: API (Controllers)

- **Name**: API Layer
- **Description**: REST controllers handling HTTP requests/responses, validation, and DTO mapping.
- **Components**:
  - `@RestController` classes
  - Request/Response DTOs
  - Mappers (domain → DTO)
- **Color**: "#4CAF50"
- **Expanded**: `true`
- **Responsibilities**:
  - HTTP request handling
  - Input validation (Bean Validation)
  - Authorization checks (@PreAuthorize)
  - Response wrapping (ApiResponse<T>)
- **Technologies**:
  - Spring MVC
  - SpringDoc OpenAPI

---

### Layer 2: Application (Handlers)

- **Name**: Application Layer
- **Description**: Use case orchestration, Command/Query handlers, transaction boundaries.
- **Components**:
  - Command handlers (@Component)
  - Query handlers (@Component)
  - Command records (immutable)
- **Color**: "#2196F3"
- **Expanded**: `true`
- **Responsibilities**:
  - Orchestrate domain logic
  - Transaction management (@Transactional)
  - Event publishing
  - Result<T> for error handling
- **Technologies**:
  - Spring Framework

---

### Layer 3: Domain

- **Name**: Domain Layer
- **Description**: Pure Java domain models, business logic, repository ports. Zero Spring annotations.
- **Components**:
  - Aggregates (Account, Transfer, Loan)
  - Value Objects (Money, AccountId, UserId)
  - Domain Services
  - Port interfaces (in/out)
- **Color**: "#FF9800"
- **Expanded**: `true`
- **Responsibilities**:
  - Business rules enforcement
  - Domain invariants
  - Pure Java (no framework dependencies)
- **Technologies**:
  - Plain Java (Java 21)

---

### Layer 4: Infrastructure

- **Name**: Infrastructure Layer
- **Description**: Adapters implementing domain ports, JPA entities, external service stubs.
- **Components**:
  - JPA Repositories (Spring Data)
  - Redis adapters
  - Event publishers/listeners
  - External service stubs (email, SMS, FX)
- **Color**: "#9C27B0"
- **Expanded**: `true`
- **Responsibilities**:
  - Implement domain ports (out)
  - Persist entities
  - Publish/consume events
  - External integrations
- **Technologies**:
  - Spring Data JPA
  - Spring Data Redis
  - Spring Kafka

---

## 2. Design Patterns (`DesignPattern[]`)

- **Title**: Hexagonal Architecture (Ports & Adapters)
- **Emoji**: "🔌"
- **Description**: Each module has its own API, Application, Domain, and Infrastructure packages. Modules communicate via shared value objects and ApplicationEvents, never directly.
- **Category**: "Architecture"
- **Badge**: "DDD"
- **GitHub Example URL**: "https://github.com/alexistrejo11/bank-api/tree/main/bank-payments/src/main/java/io/github/alexistrejo11/bank/payments"

- **Title**: Command/Query Responsibility Segregation (CQRS)
- **Emoji**: "📝"
- **Description**: Separate Command handlers (write) from Query handlers (read). Commands return Result<T>, Queries return domain models.
- **Category**: "Pattern"
- **Badge**: "Pattern"

- **Title**: Event Sourcing (Partial)
- **Emoji**: "📡"
- **Description**: Domain events (TransferCompletedEvent, LoanDisbursedEvent) drive side-effects across modules. Audit records as event log.
- **Category**: "Pattern"
- **Badge**: "Event-Driven"

- **Title**: Idempotency Pattern
- **Emoji**: "🔁"
- **Description**: Payment transfers use idempotency keys stored in Redis to prevent duplicate processing.
- **Category**: "Pattern"
- **Badge**: "Resilience"

- **Title**: Rate Limiting (Token Bucket)
- **Emoji**: "🚦"
- **Description**: Redis-backed token bucket algorithm with global and per-user limits.
- **Category**: "Pattern"
- **Badge**: "Resilience"

- **Title**: Double-Entry Bookkeeping
- **Emoji**: "📒"
- **Description**: Every balance change creates two ledger entries (debit + credit). Balance is derived, never stored.
- **Category**: "Pattern"
- **Badge**: "Finance"

---

## 3. Scalability Strategies (`StrategyItem[]`)

- **Title**: Modular Monolith Extraction Path
- **Description**: Modules are decoupled enough that any one can be extracted into a standalone microservice by replacing ApplicationEvent calls with a message broker (SQS/Kafka) and giving it its own datasource.

- **Title**: Stateless Services
- **Description**: All services are stateless; session state stored in Redis. JWT tokens contain all auth info, no server-side session.

- **Title**: Async Event Processing
- **Description**: Cross-module events processed asynchronously via @Async + @TransactionalEventListener. After_commit phase ensures reliability.

- **Title**: Database Connection Pooling
- **Description**: HikariCP (Spring Boot default) for connection pooling. PostgreSQL for ACID transactions.

---

## 4. Security Strategies (`StrategyItem[]`)

- **Title**: JWT RS256 with Asymmetric Keys
- **Description**: RSA key pair for JWT signing. Private key for issuance, public key for validation. Public key exposed at /.well-known/jwks.json.

- **Title**: RBAC (Role-Based Access Control)
- **Description**: Users have Roles → Permissions. @PreAuthorize("hasAuthority('permission:action')") on endpoints.

- **Title**: JWT Refresh Token Rotation
- **Description**: Short-lived access tokens (15 min), long-lived refresh tokens (7 days) stored in Redis. Rotation on use.

- **Title**: JWT Blocklist
- **Description**: On logout, JWT JTI added to Redis blocklist. Tokens validated against blocklist.

- **Title**: Rate Limiting
- **Description**: Per-IP global rate limit + per-user profile-based limits. 429 responses with Retry-After.

---

## 5. Cache Strategies (`CacheStrategy[]`)

- **Name**: Refresh Tokens
- **Description**: Stored in Redis with TTL (7 days). Key: refresh:{userId}:{sha256(token)}
- **TTL**: "7 days"
- **Coverage**: "IAM module - authentication"

- **Name**: JWT Blocklist
- **Description**: Stored in Redis with remaining token lifetime. Key: blocklist:{jti}
- **TTL**: "Until token expiry"
- **Coverage**: "IAM module - logout"

- **Name**: Transfer Idempotency
- **Description**: Idempotency outcomes cached in Redis. Key: idempotency:{key}
- **TTL**: "24 hours (configurable)"
- **Coverage**: "Payments module - transfers"

- **Name**: Rate Limit Buckets
- **Description**: Token bucket state in Redis. Per-IP global + per-user profile buckets.
- **TTL**: "1 second (refill rate)"
- **Coverage**: "All modules - rate limiting"

---

## 6. Architecture Features (`ArchitectureFeature[]`)

- **Title**: Modular Package Structure
- **Emoji**: "📦"
- **Description**: Each module follows identical package structure: api/, application/, domain/, infrastructure/. No cross-module Spring bean injection.

- **Title**: Domain Events
- **Emoji**: "📡"
- **Description**: TransferCompletedEvent, LoanDisbursedEvent, LoanRepaymentCompletedEvent trigger side-effects across modules.

- **Title**: Result<T> Type
- **Emoji**: "✅"
- **Description**: Sealed Result<T> interface with Success/Failure records for explicit error handling in handlers.

- **Title**: Value Objects
- **Emoji**: "💰"
- **Description**: Immutable value objects (Money, AccountId, UserId, Currency) in shared module. Strong typing across modules.

---

## 7. Architecture Diagram (`ArchitectureDiagramModel`)

### Legend (`LegendItem[]`)

- **Type**: "client"
- **Label**: "Client/Frontend"
- **Color**: "#4CAF50"
- **Icon**: "🌐"

- **Type**: "gateway"
- **Label**: "API Gateway"
- **Color**: "#2196F3"
- **Icon**: "🚪"

- **Type**: "service"
- **Label**: "Service Module"
- **Color**: "#FF9800"
- **Icon**: "⚙️"

- **Type**: "database"
- **Label**: "Database"
- **Color**: "#9C27B0"
- **Icon**: "🗄️"

- **Type**: "queue"
- **Label**: "Message Queue"
- **Color**: "#E91E63"
- **Icon**: "📨"

- **Type**: "cache"
- **Label**: "Cache"
- **Color**: "#00BCD4"
- **Icon**: "💾"

### Nodes (`DiagramNode[]`)

- **ID**: "client-1"
- **Label**: "Frontend App"
- **Type**: `client`
- **x**: 100
- **y**: 300
- **Status**: `healthy`

- **ID**: "gateway-1"
- **Label**: "Spring Boot (bank-boot)"
- **Type**: `gateway`
- **x**: 300
- **y**: 300
- **Status**: `healthy`

- **ID**: "service-iam"
- **Label**: "IAM Module"
- **Type**: `service`
- **x**: 500
- **y**: 100
- **Status**: `healthy`

- **ID**: "service-accounts"
- **Label**: "Accounts Module"
- **Type**: `service`
- **x**: 500
- **y**: 200
- **Status**: `healthy`

- **ID**: "service-payments"
- **Label**: "Payments Module"
- **Type**: `service`
- **x**: 500
- **y**: 300
- **Status**: `healthy`

- **ID**: "service-loans"
- **Label**: "Loans Module"
- **Type**: `service`
- **x**: 500
- **y**: 400
- **Status**: `healthy`

- **ID**: "service-notifications"
- **Label**: "Notifications Module"
- **Type**: `service`
- **x**: 500
- **y**: 500
- **Status**: `healthy`

- **ID**: "service-audit"
- **Label**: "Audit Module"
- **Type**: `service`
- **x**: 500
- **y**: 600
- **Status**: `healthy`

- **ID**: "db-1"
- **Label**: "PostgreSQL"
- **Type**: `database`
- **x**: 700
- **y**: 300
- **Status**: `healthy`

- **ID**: "cache-1"
- **Label**: "Redis"
- **Type**: `cache`
- **x**: 700
- **y**: 400
- **Status**: `healthy`

- **ID**: "queue-1"
- **Label**: "Kafka/Redpanda"
- **Type**: `queue`
- **x**: 700
- **y**: 500
- **Status**: `healthy`

### Connections (`DiagramConnection[]`)

- **ID**: "conn-1"
- **From**: "client-1"
- **To**: "gateway-1"
- **Label**: "HTTP/REST"
- **Protocol**: "HTTP"
- **Is Active**: `true`

- **ID**: "conn-2"
- **From**: "gateway-1"
- **To**: "service-iam"
- **Label**: "JWT Auth"
- **Protocol**: "Internal"
- **Is Active**: `true`

- **ID**: "conn-3"
- **From**: "gateway-1"
- **To**: "service-accounts"
- **Label**: "API"
- **Protocol**: "Internal"
- **Is Active**: `true`

- **ID**: "conn-4"
- **From**: "gateway-1"
- **To**: "service-payments"
- **Label**: "API"
- **Protocol**: "Internal"
- **Is Active**: `true`

- **ID**: "conn-event-1"
- **From**: "service-payments"
- **To**: "service-accounts"
- **Label**: "TransferCompletedEvent"
- **Protocol**: "ApplicationEvent"
- **Is Active**: `true`

- **ID**: "conn-event-2"
- **From**: "service-payments"
- **To**: "service-audit"
- **Label**: "TransferCompletedEvent"
- **Protocol**: "ApplicationEvent"
- **Is Active**: `true`

- **ID**: "conn-event-3"
- **From**: "service-payments"
- **To**: "service-notifications"
- **Label**: "TransferCompletedEvent"
- **Protocol**: "ApplicationEvent"
- **Is Active**: `true`

- **ID**: "conn-5"
- **From**: "service-iam"
- **To**: "cache-1"
- **Label**: "Redis"
- **Protocol**: "TCP"
- **Is Active**: `true`

- **ID**: "conn-6"
- **From**: "service-payments"
- **To**: "cache-1"
- **Label**: "Redis (Idempotency)"
- **Protocol**: "TCP"
- **Is Active**: `true`

- **ID**: "conn-7"
- **From**: "gateway-1"
- **To**: "db-1"
- **Label**: "PostgreSQL"
- **Protocol**: "JDBC"
- **Is Active**: `true`

---

## 8. Data Flow (`DataFlowModel`)

### Request Flow (`FlowStep[]`)

1. **Number**: 1
2. **Title**: "HTTP Request"
3. **Description**: "Client sends REST request with JWT in Authorization header"
4. **Icon**: "📥"

2. **Number**: 2
2. **Title**: "Security Filter"
- **Description**: "JwtAuthenticationFilter validates JWT, populates SecurityContext"
- **Icon**: "🔐"

3. **Number**: 3
- **Title**: "Controller"
- **Description**: "REST controller validates input, calls handler"
- **Icon**: "🎮"

4. **Number**: 4
- **Title**: "Handler"
- **Description**: "Command handler orchestrates domain logic, manages transaction"
- **Icon**: "⚙️"

5. **Number**: 5
- **Title**: "Domain"
- **Description**: "Domain model enforces business rules"
- **Icon**: "🏛️"

6. **Number**: 6
- **Title**: "Persistence"
- **Description**: "Infrastructure adapter persists entities to database"
- **Icon**: "💾"

7. **Number**: 7
- **Title**: "Event Publishing"
- **Description**: "Handler publishes domain event (AFTER_COMMIT)"
- **Icon**: "📡"

### Event Flow (`FlowStep[]`)

1. **Number**: 1
- **Title**: "Domain Event"
- **Description**: "TransferCompletedEvent published by Payments handler"
- **Icon**: "💸"

2. **Number**: 2
- **Title**: "Accounts Listener"
- **Description**: "AccountsEventListener posts two LedgerEntry rows"
- **Icon**: "📒"

3. **Number**: 3
- **Title**: "Audit Listener"
- **Description**: "AuditEventListener writes AuditRecord"
- **Icon**: "📋"

4. **Number**: 4
- **Title**: "Notification Listener"
- **Description**: "NotificationEventListener queues email to sender/receiver"
- **Icon**: "📧"

---

## 9. Tech Decisions (`TechDecisionsModel`)

- **Title**: Modular Monolith over Microservices
  - **Problem**: Need scalability but team is small, microservices overhead too high
  - **Solution**: Build as modular monolith with clean boundaries. Extract to microservices later when needed.
  - **Alternatives**:
    - Full microservices (too complex for team size)
    - Single monolith (no extraction path)
  - **Outcome**: "Modules can be extracted to standalone services by replacing ApplicationEvents with message broker"
  - **Icon**: "📦"

- **Title**: Hexagonal Architecture
  - **Problem**: Need testable domain logic without Spring dependencies
  - **Solution**: Separate domain from infrastructure with ports. Domain layer is pure Java.
  - **Alternatives**:
    - Layered architecture (tighter coupling)
    - Pure DDD (more complex)
  - **Outcome**: "Domain logic testable without Spring context"
  - **Icon**: "🔌"

- **Title**: PostgreSQL over MySQL
  - **Problem**: Need ACID for financial transactions, JSONB for audit payloads
  - **Solution**: PostgreSQL with JSONB support and strong ACID guarantees
  - **Alternatives**:
    - MySQL (weaker JSON support)
    - NoSQL (no ACID)
  - **Outcome**: "Reliable financial transactions, flexible audit storage"
  - **Icon**: "🐘"

- **Title**: Redis for Sessions and Idempotency
  - **Problem**: Need fast token storage and deduplication
  - **Solution**: Redis for refresh tokens, JWT blocklist, idempotency cache
  - **Alternatives**:
    - Database sessions (slower)
    - In-memory (not persistent)
  - **Outcome**: "Fast, persistent token management"
  - **Icon**: "💾"

- **Title**: ApplicationEvent over Kafka (Default)
  - **Problem**: Need decoupled cross-module communication without infrastructure overhead
  - **Solution**: Spring ApplicationEvent for in-process events, Kafka-ready (swap surface is @EventListener)
  - **Alternatives**:
    - Direct bean calls (tight coupling)
    - Kafka from start (overhead for dev)
  - **Outcome**: "Simple in-proc events, Kafka available in docker profile"
  - **Icon**: "📡"

- **Title**: JWT RS256 over HS256
  - **Problem**: Need service-to-service trust, key rotation
  - **Solution**: Asymmetric RS256, public key at /.well-known/jwks.json
  - **Alternatives**:
    - HS256 (shared secret, less secure)
    - Opaque tokens (need introspection endpoint)
  - **Outcome**: "Future-proof for service-to-service auth"
  - **Icon**: "🔐"
