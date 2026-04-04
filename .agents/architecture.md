# ARCHITECTURE.md

## Architectural style
Modular monolith. Each domain module is a Gradle subproject with strict internal layering.
Modules are decoupled enough that any one can be extracted into a standalone microservice
by replacing `ApplicationEvent` calls with a message broker (SQS/Kafka) and giving it its
own datasource. No module knows about the internals of another.

## Internal package structure (apply to every module)
```
io.github.alexistrejo11.bank.{module}
  ├── api/
  │   ├── controller/       # @RestController classes only — no business logic
  │   ├── dto/
  │   │   ├── request/      # one record per endpoint input
  │   │   └── response/     # one record per endpoint output
  │   └── mapper/           # maps domain → response DTO (no MapStruct; manual mappers)
  ├── application/
  │   ├── command/          # Command records (TransferFundsCommand)
  │   ├── query/            # Query records (GetAccountByIdQuery)
  │   └── handler/
  │       ├── command/      # @Component command handlers (TransferFundsHandler)
  │       └── query/        # @Component query handlers (GetAccountByIdHandler)
  ├── domain/
  │   ├── model/            # Aggregates, entities, value objects — pure Java, zero Spring
  │   ├── port/
  │   │   ├── in/
  │   │   │   ├── command/  # Command use case interfaces
  │   │   │   └── query/    # Query use case interfaces
  │   │   └── out/          # Repository / external service interfaces (command + query)
  │   └── service/          # Domain services (stateless, pure logic)
  └── infrastructure/
      ├── persistence/
      │   ├── entity/       # JPA @Entity classes (separate from domain model)
      │   ├── repository/   # Spring Data JPA interfaces
      │   └── adapter/      # Implements domain port.out interfaces (writes + reads)
      ├── events/
      │   ├── publisher/    # Wraps ApplicationEventPublisher
      │   └── listener/     # @TransactionalEventListener handlers
      └── external/         # Third-party stubs (email, SMS, FX rate)
```

## Layer dependency rules
```
api → application → domain ← infrastructure
```
- `api` depends on `application` and `domain` (DTOs + commands)
- `application` (handlers) depends on `domain` (ports + model)
- `infrastructure` depends on `domain` (implements ports) — never the reverse
- `domain` depends only on `shared` — no Spring, no JPA, no external libs

## Cross-module communication
Only two legal channels:

**1. ApplicationEvent (async, fire-and-forget)**
```java
// Publisher (inside the originating module)
eventPublisher.publishEvent(new TransferCompletedEvent(transferId, sourceId, targetId, amount));

// Listener (inside the receiving module)
@Component
public class AccountsTransferListener {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransferCompletedEvent event) { ... }
}
```
Events are defined in `shared` module. AFTER_COMMIT guarantees side-effects only fire on success.

**2. Shared value objects**
`shared` exposes records like `Money`, `AccountId`, `UserId`, `Currency`. Any module may use these.
No module may expose its own domain entities to another module.

## Security architecture
```
HTTP Request
  └─► Spring Security Filter Chain
        ├── JwtAuthenticationFilter   (validates RS256 JWT, populates SecurityContext)
        ├── RateLimitingFilter        (Redis counter per IP + userId)
        └── AuthorizationFilter       (@PreAuthorize checks permissions[])

JWT Claims: { sub: userId, roles: [...], permissions: [...], jti, iat, exp }
Key pair: RSA-2048, loaded from env (BANK_JWT_PRIVATE_KEY / BANK_JWT_PUBLIC_KEY)
Public key exposed at: GET /.well-known/jwks.json
Refresh token: stored in Redis as  refresh:{userId}:{sha256(token)}  TTL=7d
JWT blocklist:  stored in Redis as  blocklist:{jti}  TTL=remaining token lifetime
```

## Observability architecture
```
Spring Boot App
  ├── /actuator/prometheus  ──► Prometheus ──► Grafana (dashboards)
  └── Logback (JSON encoder)
        └── stdout ──► Logstash ──► Elasticsearch ──► Kibana
```
Every log line must carry MDC fields: `traceId`, `spanId`, `userId`, `module`, `requestId`.
Custom metrics registered via `MeterRegistry` in each module's application layer.

## Docker Compose service map
| Service | Image | Port |
|---|---|---|
| `app` | local build | 8080 |
| `postgres` | postgres:16-alpine | 5432 |
| `redis` | redis:7-alpine | 6379 |
| `prometheus` | prom/prometheus | 9090 |
| `grafana` | grafana/grafana | 3000 |
| `elasticsearch` | elasticsearch:8.x | 9200 |
| `logstash` | logstash:8.x | 5044 |
| `kibana` | kibana:8.x | 5601 |

`app` health-depends on `postgres` and `redis`. All other services are optional for dev.

## AWS migration path (when needed)
1. Extract one module → standalone Spring Boot service (package boundaries are already clean)
2. Replace `ApplicationEventPublisher` → Amazon SQS/SNS or Kafka
3. Each service gets its own RDS schema (already isolated by module)
4. Add AWS API Gateway or Spring Cloud Gateway as ingress
5. Redis → ElastiCache · Prometheus/Grafana → CloudWatch + Managed Grafana · ELK → OpenSearch

## Implementation order
1. Gradle multi-project scaffold + Docker Compose skeleton + Flyway baseline
2. `shared` module — Money, AccountId, Result<T>, base exceptions, events
3. `iam` — auth, JWT, RBAC (all other modules depend on this)
4. `accounts` — account CRUD, double-entry ledger
5. `audit` — event listener infrastructure (wire up early so it captures everything)
6. `payments` — transfers, idempotency, state machine
7. `loans` — origination, schedule, repayment
8. `notifications` — templates, async dispatch
9. Observability wiring, seed data, Swagger UI polish