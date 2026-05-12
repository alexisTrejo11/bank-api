---
codeExamples:
  - id: "security-authority-mapping"
    title: "Route-level RBAC with Spring Security 6"
    description: "Shows how public auth routes are permitAll, banking routes require explicit authorities, and JwtAuthenticationFilter is registered ahead of UsernamePasswordAuthenticationFilter."
    category: "security"
    duration: "3 min read"
    views: 0
    tags:
      - "Spring Security"
      - "JWT"
      - "RBAC"
    files:
      - name: "SecurityConfig.java"
        path: "bank-config/src/main/java/io/github/alexisTrejo11/bank/security/SecurityConfig.java"
        language: "java"
        highlighted: true
        explanation: "Second filter chain secures /api/** with authority rules; actuator chain is separate and fully permitted — lock down on AWS."
        content: |
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
  - id: "double-entry-ledger-handler"
    title: "Paired ledger rows in one transaction"
    description: "PostTransferToLedgerHandler loads both accounts, validates currency and status, then saves a DEBIT and CREDIT with the same business reference id."
    category: "domain"
    duration: "4 min read"
    views: 0
    tags:
      - "DDD"
      - "Ledger"
      - "Transactional"
    files:
      - name: "PostTransferToLedgerHandler.java"
        path: "bank-accounts/src/main/java/io/github/alexistrejo11/bank/accounts/application/handler/command/PostTransferToLedgerHandler.java"
        language: "java"
        highlighted: true
        explanation: "ledgerEntryRepository.savePair keeps the invariant that both legs exist or neither does."
        content: |
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
  - id: "payments-idempotency-header"
    title: "Transfer API with required Idempotency-Key"
    description: "InitiateTransferHandler is invoked with a UUID idempotency key header so Redis can deduplicate retries from mobile or BFF clients."
    category: "api"
    duration: "2 min read"
    views: 0
    tags:
      - "Payments"
      - "Idempotency"
      - "REST"
    files:
      - name: "TransferController.java"
        path: "bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/presentation/controller/TransferController.java"
        language: "java"
        highlighted: true
        explanation: "Missing header yields framework-level 400; duplicate keys short-circuit to stored outcome when implemented in handler."
        content: |
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
---

# Code showcase

## Notes

- **Danger**: The `double-entry-ledger-handler` excerpt omits variable declarations (`ccy`, `ref`, `now`) for brevity — treat it as illustrative; use the repository file for full context.
- **Good**: Linking `path` to real repository files keeps this portfolio section honest for reviewers.
- **Missing**: Add a fourth example for **Kafka consumer** or **@TransactionalEventListener** once you stabilize MSK consumer code in-repo.
- **Observation**: When publishing this YAML to a site that renders code blocks, ensure the renderer supports literal block scalars (`|`) inside front matter or move large `content` fields to external gist URLs later.
