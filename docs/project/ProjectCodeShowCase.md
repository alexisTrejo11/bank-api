# Code showcase

Illustrative excerpts tied to real repository paths. Source: [`source/ProjectCodeShowCase.md`](source/ProjectCodeShowCase.md). For full implementations open the linked files in the repo — snippets here may omit imports or local variables for readability.

---

## 1. Route-level RBAC with Spring Security 6

| Field | Value |
|--------|--------|
| **ID** | `security-authority-mapping` |
| **Category** | security |
| **Read time** | ~3 min |
| **Tags** | Spring Security, JWT, RBAC |

**Intent:** Show how public auth routes are `permitAll`, banking routes require explicit **authorities**, and `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`.

**File:** [`bank-config/src/main/java/io/github/alexisTrejo11/bank/security/SecurityConfig.java`](../../bank-config/src/main/java/io/github/alexisTrejo11/bank/security/SecurityConfig.java)

**Excerpt (illustrative — not a full class):**

```java
@Bean
@Order(2)
SecurityFilterChain apiSecurityFilterChain(
    HttpSecurity http,
    JwtAuthenticationFilter jwtAuthenticationFilter,
    CorsConfigurationSource bankCorsConfigurationSource) throws Exception {
  return http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(c -> c.configurationSource(bankCorsConfigurationSource))
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
          .requestMatchers(
              "/api/v1/auth/register",
              "/api/v1/auth/login",
              "/api/v1/auth/refresh",
              "/swagger-ui/**",
              "/v3/api-docs/**",
              "/.well-known/jwks.json")
          .permitAll()
          .requestMatchers(HttpMethod.POST, "/api/v1/payments/transfers")
          .hasAuthority("payments:write")
          .requestMatchers(HttpMethod.GET, "/api/v1/audit/records")
          .hasAuthority("audit:read")
          .anyRequest().authenticated())
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
}
```

**Note:** The real `SecurityConfig` lists every banking route; the excerpt is shortened. Actuator uses a **separate** filter chain that permits all `/actuator/**` — lock down at the **network** layer on AWS.

---

## 2. Paired ledger rows in one transaction

| Field | Value |
|--------|--------|
| **ID** | `double-entry-ledger-handler` |
| **Category** | domain |
| **Read time** | ~4 min |
| **Tags** | DDD, Ledger, Transactional |

**Intent:** `PostTransferToLedgerHandler` loads both accounts, validates **currency** and **status**, then persists a **DEBIT** and **CREDIT** sharing the same business reference id via `savePair`.

**File:** [`bank-accounts/src/main/java/io/github/alexistrejo11/bank/accounts/application/handler/command/PostTransferToLedgerHandler.java`](../../bank-accounts/src/main/java/io/github/alexistrejo11/bank/accounts/application/handler/command/PostTransferToLedgerHandler.java)

**Excerpt (abbreviated — `ccy`, `ref`, `now` omitted here; see repo for full method):**

```java
@Transactional
public void execute(PostTransferToLedgerCommand command) {
  BankAccount fromAcc = accountRepository.findById(command.from().value())
      .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
  BankAccount toAcc = accountRepository.findById(command.to().value())
      .orElseThrow(() -> new AccountNotFoundException("Target account not found"));
  if (!fromAcc.currency().equals(ccy) || !toAcc.currency().equals(ccy)) {
    throw new InvalidTransferException("Currency mismatch between accounts and transfer");
  }
  LedgerEntry debit = new LedgerEntry(
      UUID.randomUUID(), command.from().value(), LedgerEntryType.DEBIT,
      command.amount(), ccy, command.referenceType(), ref, now);
  LedgerEntry credit = new LedgerEntry(
      UUID.randomUUID(), command.to().value(), LedgerEntryType.CREDIT,
      command.amount(), ccy, command.referenceType(), ref, now);
  ledgerEntryRepository.savePair(debit, credit);
}
```

---

## 3. Transfer API with required `Idempotency-Key`

| Field | Value |
|--------|--------|
| **ID** | `payments-idempotency-header` |
| **Category** | api |
| **Read time** | ~2 min |
| **Tags** | Payments, Idempotency, REST |

**Intent:** `InitiateTransferHandler` receives a **UUID** idempotency header so Redis can deduplicate client retries.

**File:** [`bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/presentation/controller/TransferController.java`](../../bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/presentation/controller/TransferController.java)

**Excerpt:**

```java
@PostMapping("/transfers")
public ResponseEntity<ApiResponse<?>> transfer(
    @AuthenticationPrincipal IamUserPrincipal principal,
    @RequestHeader("Idempotency-Key") UUID idempotencyKey,
    @Valid @RequestBody TransferFundsRequest request) {
  var command = new InitiateTransferCommand(
      principal.userId(), idempotencyKey,
      request.sourceAccountId(), request.targetAccountId(),
      request.amount(), request.currency());
  Result<TransferResponse> result = initiateTransferHandler.handle(command);
  return toResponse(result);
}
```

---

## Follow-ups

- Add a **fourth** showcase for **Kafka consumer** or **`@TransactionalEventListener(AFTER_COMMIT)`** once MSK consumer code is stable in-tree.  
- Prefer linking to **line ranges** on GitHub for code review deep links.

## Related

- [ProjectArchitectureModel.md](ProjectArchitectureModel.md) — where these pieces sit  
- [APISchema.md](APISchema.md) — HTTP contract for transfers  
