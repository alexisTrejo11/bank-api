# Code Showcase

## Code Examples (`CodeExample[]`)

### Example 1: Hexagonal Architecture Structure

- **ID**: "code-001"
- **Title**: "Module Package Structure"
- **Description**: "Example showing the hexagonal architecture package structure in bank-payments module"
- **Category**: "Architecture"
- **Duration**: "N/A"
- **Views**: 0
- **Tags**: `["DDD", "Hexagonal", "Ports & Adapters"]`

#### Files (`CodeFile[]`)

- **Name**: "Application Layer"
- **Path**: "bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/application/handler/command/InitiateTransferHandler.java"
- **Language**: "java"
- **Content**:
  ```java
  package io.github.alexistrejo11.bank.payments.application.handler.command;

  @Component
  @RequiredArgsConstructor
  public class InitiateTransferHandler {
      private final TransferRepository transferRepository;
      private final AccountRepository accountRepository;
      private final TransferIdempotencyPort idempotencyPort;
      private final ApplicationEventPublisher eventPublisher;

      @Transactional
      public Result<TransferResponse> handle(InitiateTransferCommand command) {
          // 1. Idempotency check
          var cached = idempotencyPort.getCachedOutcome(command.idempotencyKey());
          if (cached.isPresent()) {
              return cached.get();
          }
          // 2. Load accounts
          var source = accountRepository.findById(command.sourceId())
              .orElseThrow(() -> new AccountNotFoundException(command.sourceId()));
          var target = accountRepository.findById(command.targetId())
              .orElseThrow(() -> new AccountNotFoundException(command.targetId()));
          // 3. Domain logic
          var transfer = Transfer.initiate(source, target, command.amount(), command.idempotencyKey());
          // 4. Persist
          var saved = transferRepository.save(transfer);
          // 5. Publish event
          eventPublisher.publishEvent(new TransferCompletedEvent(
              saved.getId(), source.getId(), target.getId(), command.amount()
          ));
          // 6. Cache outcome
          idempotencyPort.cacheOutcome(command.idempotencyKey(), Result.success(toResponse(saved)));
          return Result.success(toResponse(saved));
      }
  }
  ```
- **Highlighted**: `true`
- **Explanation**: "Shows Command handler pattern, idempotency check, domain logic, event publishing"

---

### Example 2: Double-Entry Ledger

- **ID**: "code-002"
- **Title**: "Ledger Posting via Domain Events"
- **Description**: "How TransferCompletedEvent triggers ledger entries in accounts module"
- **Category**: "Domain"
- **Tags**: `["DDD", "Event-Driven", "Ledger"]`

#### Files (`CodeFile[]`)

- **Name**: "Event Listener"
- **Path**: "bank-accounts/src/main/java/io/github/alexistrejo11/bank/accounts/infrastructure/event/AccountsTransferListener.java"
- **Language**: "java"
- **Content**:
  ```java
  package io.github.alexistrejo11.bank.accounts.infrastructure.event;

  @Component
  public class AccountsTransferListener {
      private final PostTransferToLedgerUseCase postTransferToLedgerUseCase;

      @Async
      @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
      public void on(TransferCompletedEvent event) {
          postTransferToLedgerUseCase.post(
              event.transferId(),
              event.sourceAccountId(),
              event.targetAccountId(),
              event.amount()
          );
      }
  }
  ```
- **Highlighted**: `true`
- **Explanation**: "AFTER_COMMIT ensures ledger posts only on successful transfer commit"

- **Name**: "Use Case Interface"
- **Path**: "bank-accounts/src/main/java/io/github/alexistrejo11/bank/accounts/domain/port/in/command/PostTransferToLedgerUseCase.java"
- **Language**: "java"
- **Content**:
  ```java
  package io.github.alexistrejo11.bank.accounts.domain.port.in.command;

  public interface PostTransferToLedgerUseCase {
      void post(TransferId transferId, AccountId sourceId, AccountId targetId, Money amount);
  }
  ```

---

### Example 3: JWT Token Service

- **ID**: "code-003"
- **Title**: "RS256 JWT Authentication"
- **Description**: "JWT generation with RS256 asymmetric keys"
- **Category**: "Security"
- **Tags**: `["JWT", "RS256", "Spring Security"]`

#### Files (`CodeFile[]`)

- **Name**: "JwtTokenService"
- **Path**: "bank-iam/src/main/java/io/github/alexistrejo11/bank/iam/infrastructure/security/JwtTokenService.java"
- **Language**: "java"
- **Content**:
  ```java
  @Component
  @RequiredArgsConstructor
  public class JwtTokenService {
      private final RSAPrivateKey privateKey;
      private final RSAPublicKey publicKey;
      private final JwtBlocklistStore blocklistStore;

      public String generateAccessToken(User user) {
          var now = Instant.now();
          var expiry = now.plus(Duration.ofMinutes(15));
          var jti = UUID.randomUUID().toString();

          return Jwts.builder()
              .id(jti)
              .subject(user.getId().toString())
              .claim("roles", user.getRoles().stream().map(Role::getName).toList())
              .claim("permissions", user.getPermissions().stream().map(Permission::getName).toList())
              .issuedAt(now)
              .expiration(expiry)
              .signWith(privateKey, JWSAlgorithm.RS256)
              .compact();
      }

      public boolean validateToken(String token) {
          try {
              var jws = Jwts.parser()
                  .verifyWith(publicKey)
                  .build()
                  .parseSignedClaims(token);
              var jti = jws.getPayload().getId();
              return !blocklistStore.isBlocked(jti);
          } catch (JwtException e) {
              return false;
          }
      }
  }
  ```
- **Highlighted**: `true`
- **Explanation**: "RS256 asymmetric signing, JTI for blocklist, claims for RBAC"

---

### Example 4: Rate Limiting Annotation

- **ID**: "code-004"
- **Title**: "Per-Endpoint Rate Limiting"
- **Description**: "Using @RateLimit annotation on sensitive endpoints"
- **Category**: "Security"
- **Tags**: `["Rate Limiting", "Redis"]`

#### Files (`CodeFile[]`)

- **Name**: "Transfer Controller with Rate Limit"
- **Path**: "bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/api/controller/TransferController.java"
- **Language**: "java"
- **Content**:
  ```java
  @RestController
  @RequestMapping("/api/v1/transfers")
  @RequiredArgsConstructor
  @Tag(name = "Payments")
  public class TransferController {
      private final InitiateTransferHandler handler;

      @PostMapping
      @PreAuthorize("hasAuthority('payments:write')")
      @RateLimit(profile = RateLimitProfile.SENSITIVE_OPERATIONS)
      public ResponseEntity<ApiResponse<TransferResponse>> transfer(
          @RequestHeader("Idempotency-Key") String idempotencyKey,
          @Valid @RequestBody TransferFundsRequest request
      ) {
          var command = new InitiateTransferCommand(
              AccountId.of(request.sourceAccountId()),
              AccountId.of(request.targetAccountId()),
              new Money(request.amount(), Currency.of(request.currency())),
              idempotencyKey
          );
          var result = handler.handle(command);
          // ... response handling
      }
  }
  ```
- **Highlighted**: `true`
- **Explanation**: "@RateLimit profile='sensitive_operations' limits to 6 req/min"

---

### Example 5: Result Type for Error Handling

- **ID**: "code-005"
- **Title**: "Explicit Error Handling with Result<T>"
- **Description**: "Using sealed Result<T> interface for recoverable business errors"
- **Category**: "Pattern"
- **Tags**: `["Error Handling", "Functional"]`

#### Files (`CodeFile[]`)

- **Name**: "Result Interface"
- **Path**: "bank-shared/src/main/java/io/github/alexistrejo11/bank/shared/result/Result.java"
- **Language**: "java"
- **Content**:
  ```java
  public sealed interface Result<T> permits Result.Success, Result.Failure {
      record Success<T>(T value) implements Result<T> {
          public boolean isSuccess() { return true; }
      }
      record Failure<T>(String code, String message) implements Result<T> {
          public boolean isSuccess() { return false; }
      }

      static <T> Result<T> success(T value) { return new Success<>(value); }
      static <T> Result<T> failure(String code, String message) { return new Failure<>(code, message); }
  }
  ```

- **Name**: "Usage in Handler"
- **Path**: "bank-payments/src/main/java/.../InitiateTransferHandler.java (excerpt)"
- **Language**: "java"
- **Content**:
  ```java
  // In handler
  if (!source.hasSufficientFunds(amount)) {
      return Result.failure("INSUFFICIENT_FUNDS", "Account balance is below transfer amount");
  }
  return Result.success(toResponse(saved));

  // In controller
  var result = handler.handle(command);
  if (!result.isSuccess()) {
      var failure = (Result.Failure<?>) result;
      return ResponseEntity.unprocessableEntity()
          .body(ApiResponse.failure(failure.code(), failure.message()));
  }
  return ResponseEntity.ok(ApiResponse.success(((Result.Success<TransferResponse>) result).value()));
  ```

---

### Example 6: Event-Driven Audit

- **ID**: "code-006"
- **Title**: "Immutable Audit Records"
- **Description**: "AuditEventListener writes immutable audit records on domain events"
- **Category**: "Observability"
- **Tags**: `["Audit", "Event-Driven"]`

#### Files (`CodeFile[]`)

- **Name**: "Audit Listener"
- **Path**: "bank-audit/src/main/java/io/github/alexistrejo11/bank/audit/infrastructure/event/AuditEventListener.java"
- **Language**: "java"
- **Content**:
  ```java
  @Component
  @RequiredArgsConstructor
  public class AuditEventListener {
      private final AppendAuditRecordUseCase appendAuditRecordUseCase;

      @Async
      @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
      public void on(BankDomainEvent event) {
          var command = new AppendAuditRecordCommand(
              event.getClass().getSimpleName(),
              event.actorId(),
              event.entityType(),
              event.entityId(),
              event.toJson()
          );
          appendAuditRecordUseCase.append(command);
      }
  }
  ```
