# CONVENTIONS.md

## Package & class naming
| Artifact | Pattern | Example |
|---|---|---|
| Command | `{Verb}{Noun}Command` (record) | `TransferFundsCommand` |
| Handler | `{Verb}{Noun}Handler` (@Component) | `TransferFundsHandler` |
| Request DTO | `{Noun}{Action}Request` (record) | `TransferFundsRequest` |
| Response DTO | `{Noun}Response` (record) | `TransferResponse` |
| Domain entity | `{Noun}` (no suffix) | `Transfer`, `Account` |
| JPA entity | `{Noun}Entity` | `TransferEntity` |
| Repository port | `{Noun}Repository` (interface in domain) | `TransferRepository` |
| JPA adapter | `{Noun}RepositoryAdapter` | `TransferRepositoryAdapter` |
| REST controller | `{Noun}Controller` | `TransferController` |
| Domain event | `{Noun}{PastVerb}Event` (record) | `TransferCompletedEvent` |
| Exception | `{Noun}{Problem}Exception` | `AccountNotFoundException` |

## Command / Handler pattern
```java
// Command — a plain record in application/command/
public record TransferFundsCommand(
    AccountId sourceId,
    AccountId targetId,
    Money amount,
    String idempotencyKey
) {}

// Handler — orchestrates, delegates to domain, publishes events
@Component
@RequiredArgsConstructor
public class TransferFundsHandler {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferEventPublisher eventPublisher;

    @Transactional
    public Result<TransferResponse> handle(TransferFundsCommand command) {
        // 1. guard / idempotency check
        // 2. load aggregates
        // 3. call domain logic
        // 4. persist
        // 5. publish event
        // 6. return Result.success(response)
    }
}
```
Handlers are the only place `@Transactional` lives. Controllers never call repositories directly.

## Result<T> — use for recoverable business errors
```java
// Definition lives in shared module
public sealed interface Result<T> permits Result.Success, Result.Failure {
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String code, String message) implements Result<T> {}

    static <T> Result<T> success(T value) { return new Success<>(value); }
    static <T> Result<T> failure(String code, String message) { return new Failure<>(code, message); }

    default boolean isSuccess() { return this instanceof Success; }
}

// Usage in handler
return Result.failure("INSUFFICIENT_FUNDS", "Account balance is below the transfer amount");

// Usage in controller — always unwrap Result before responding
Result<TransferResponse> result = handler.handle(command);
if (!result.isSuccess()) {
    Result.Failure<?> f = (Result.Failure<?>) result;
    return ResponseEntity.unprocessableEntity().body(ApiResponse.failure(f.code(), f.message()));
}
return ResponseEntity.ok(ApiResponse.success(((Result.Success<TransferResponse>) result).value()));
```

Use `Result<T>` when: insufficient funds, account not active, loan already paid off, duplicate idempotency key.
Use exceptions when: entity not found (404), invalid JWT (401), unexpected DB error (500).

## Exception hierarchy (per module)
```java
// Base in shared module
public abstract class BankException extends RuntimeException {
    private final String errorCode;
    public BankException(String errorCode, String message) { ... }
}

// Module-specific — lives in domain/
public class AccountNotFoundException extends BankException {
    public AccountNotFoundException(AccountId id) {
        super("ACCOUNT_NOT_FOUND", "Account " + id.value() + " does not exist");
    }
}
public class InsufficientFundsException extends BankException { ... }
```

## Global exception handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)   // and other Not Found variants
    public ProblemDetail handleNotFound(BankException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setProperty("errorCode", ex.getErrorCode());
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(BankException.class)
    public ProblemDetail handleBankException(BankException ex) { ... }  // 400

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(...) { ... }  // 422 with field errors

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) { ... }  // 500, log full stack
}
```
Error response format: RFC 7807 `ProblemDetail` (Spring 6 built-in). Fields: `type`, `title`, `status`, `detail`, `instance`, `errorCode`.

## API response envelope
```java
// All successful responses wrapped in ApiResponse<T>  (shared module)
public record ApiResponse<T>(T data, Meta meta, List<ApiError> errors) {
    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> failure(String code, String message) { ... }

    public record Meta(Instant timestamp, String requestId) {}
    public record ApiError(String code, String message, String field) {}
}
```

## REST controller rules
```java
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class TransferController {

    private final TransferFundsHandler handler;
    private final TransferQueryHandler queryHandler;

    @PostMapping("/transfers")
    @PreAuthorize("hasAuthority('payments:write')")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody TransferFundsRequest request
    ) { ... }
}
```
- Controllers only: validate input, call handler, map result to HTTP response. Zero business logic.
- Always use `@PreAuthorize` with permission strings (not role strings).
- Idempotency header required on every POST that mutates financial state.

## Validation rules
```java
// Layer 1 — Jakarta Bean Validation on request DTOs
public record TransferFundsRequest(
    @NotNull UUID sourceAccountId,
    @NotNull UUID targetAccountId,
    @NotNull @Positive BigDecimal amount,
    @NotBlank @Size(max = 3) String currency
) {}

// Layer 2 — Domain invariant guards inside aggregates (throw domain exceptions)
public class Money {
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidMoneyAmountException(amount);
        ...
    }
}
```
Never duplicate validation logic between layers — Bean Validation for HTTP input shape,
domain guards for business invariants.

## Database & Flyway conventions
- Migration files: `V{n}__{snake_case_description}.sql` (e.g. `V3__create_transfers_table.sql`)
- Seed file: `R__seed_demo_data.sql` (repeatable, runs when checksum changes)
- All tables use `UUID` primary keys (generated in Java, not DB serial)
- All tables have `created_at TIMESTAMPTZ DEFAULT now()` and `updated_at TIMESTAMPTZ`
- Soft delete via `deleted_at TIMESTAMPTZ NULL` — never hard delete financial records
- `audit_records` has no `updated_at` — it is insert-only by design

## JPA entity rules
- JPA `@Entity` classes live in `infrastructure/persistence/entity/` — never in `domain/model/`
- Domain model and JPA entity are separate classes. The adapter maps between them.
- Use `@Column(nullable = false)` on every non-nullable field — don't rely on DB defaults alone.
- Enums stored as `@Enumerated(EnumType.STRING)` always.
- No bidirectional `@OneToMany` — always navigate from parent to child with a query.

## Configuration and secrets
- **Never** commit real credentials, JWT private keys, or API keys. Use environment variables or a local `.env` (gitignored); document keys in `.env.example` (v0.2.0 — see `docs/v0.2.0/ROADMAP.md` §8).
- Spring Boot reads `SPRING_*` and custom `BANK_*` prefixes from the environment; keep `application.yaml` free of production secrets.

## Logging conventions
```java
// Always use SLF4J, never System.out
private static final Logger log = LoggerFactory.getLogger(TransferFundsHandler.class);

// Always include context — no bare messages
log.info("Transfer initiated transferId={} sourceId={} amount={}", id, sourceId, amount);
log.error("Transfer failed transferId={} reason={}", id, ex.getMessage(), ex);
```
MDC fields set by filter: `traceId`, `spanId`, `userId`, `module`, `requestId`.
Never log raw passwords, card numbers, full JWT tokens, or PII beyond userId.

**v0.2.0:** Prefer **JSON log output** for profiles aimed at ELK; keep field names stable so Filebeat/Logstash parsers do not churn (see `.agents/architecture.md` observability section).

## Testing conventions
```java
// Unit test — no Spring context
class TransferFundsHandlerTest {
    @Mock TransferRepository repo;
    @InjectMocks TransferFundsHandler handler;
    // test domain logic only
}

// Integration test — full context + Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class TransferControllerIT {
    @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine");
    // test full HTTP → DB round-trips via REST Assured
}
```
- Unit test class: `{Class}Test`
- Integration test class: `{Class}IT`
- Test method name: `should_{expected}_{when}_{condition}` (e.g. `should_fail_when_balance_insufficient`)
- Use `@DisplayName` for human-readable test names in reports.

## Git conventions
**Branch model (Gitflow):**
```
main          ← production-ready, tagged releases
develop       ← integration branch
feature/{ticket}-{short-desc}   ← new features
bugfix/{ticket}-{short-desc}    ← bug fixes
release/{version}               ← release prep
hotfix/{ticket}-{short-desc}    ← urgent prod fixes
```

**Commit messages (Conventional Commits):**
```
feat(payments): add idempotency key validation
fix(iam): correct JWT expiry calculation
chore(infra): add redis healthcheck to compose
refactor(accounts): extract ledger posting to domain service
test(loans): add integration test for repayment schedule
docs(api): update OpenAPI spec for transfer endpoint
```
Scope = module name. Breaking changes: add `!` after scope (`feat(iam)!: ...`) and a `BREAKING CHANGE:` footer.

**Monorepo (optional but recommended for large refactors):** Make **one commit per `bank-*` module** on `develop` (include root `pom.xml` only when that commit changes the parent). Then **cherry-pick** each commit onto the matching `feature/<module>-module` branch so PRs stay module-scoped. All commit and PR text in **English**. Full steps: `docs/MONOREPO_ATOMIC_COMMITS.md`.

## Custom Micrometer metrics (register one per module)
```java
// In handler or application service — not in domain
counter = Counter.builder("bank.transfer.total")
    .tag("status", "completed")
    .register(meterRegistry);
DistributionSummary.builder("bank.transfer.amount")
    .baseUnit("cents")
    .register(meterRegistry);
```
Metric naming: `bank.{module}.{noun}` — always namespaced, always tagged with `status`.