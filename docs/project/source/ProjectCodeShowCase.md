---
codeExamples:
  - id: "api-response-envelope"
    title: "Standardized API response envelope"
    description: "All REST endpoints return ApiResponse<T> with data, meta.timestamp, and errors[] for logical failures."
    category: "api"
    duration: "2 min read"
    views: 0
    tags:
      - "api"
      - "shared-kernel"
      - "openapi"
    files:
      - name: "ApiResponse.java"
        path: "bank-shared/src/main/java/io/github/alexistrejo11/bank/shared/shared_kernel/api/ApiResponse.java"
        language: "java"
        highlighted: true
        explanation: "Success and failure factories keep controller responses consistent across modules."
        content: |
          public record ApiResponse<T>(T data, Meta meta, List<ApiError> errors) {
              public static <T> ApiResponse<T> success(T data) {
                  return new ApiResponse<>(data, new Meta(Instant.now(), null), List.of());
              }
              public static <T> ApiResponse<T> failure(String code, String message) {
                  return new ApiResponse<>(null, new Meta(Instant.now(), null),
                      List.of(new ApiError(code, message, null)));
              }
          }

  - id: "transfer-idempotency"
    title: "Transfer initiation with idempotency"
    description: "Payments check Redis cache and DB before processing — duplicate Idempotency-Key returns cached Result."
    category: "domain"
    duration: "4 min read"
    views: 0
    tags:
      - "payments"
      - "idempotency"
      - "redis"
    files:
      - name: "InitiateTransferHandler.java"
        path: "bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/application/handler/InitiateTransferHandler.java"
        language: "java"
        highlighted: true
        explanation: "Idempotency-Key header is required; self-transfer and currency rules enforced before state transition."
        content: |
          @Transactional
          public Result<TransferResponse> handle(InitiateTransferCommand command) {
              Optional<Result<TransferResponse>> cached = idempotencyCache.get(userId, idempotencyKey);
              if (cached.isPresent()) {
                  return cached.get();
              }
              if (sourceAccountId.equals(targetAccountId)) {
                  return persistFailure(command, currencyRaw, "SELF_TRANSFER",
                      "Source and target account must differ");
              }
              // ... balance check, state machine, TransferCompletedEvent
          }

  - id: "redis-rate-limit"
    title: "Redis token-bucket rate limiter"
    description: "Atomic Lua script refill + consume in one round trip; fail-open when Redis errors if configured."
    category: "security"
    duration: "3 min read"
    views: 0
    tags:
      - "redis"
      - "rate-limit"
      - "lua"
    files:
      - name: "RedisTokenBucketRateLimiter.java"
        path: "bank-boot/src/main/java/io/github/alexistrejo11/bank/infrastructure/ratelimit/RedisTokenBucketRateLimiter.java"
        language: "java"
        highlighted: true
        explanation: "Used by GlobalRateLimitFilter and AnnotatedRateLimitInterceptor in docker/AWS profile."
        content: |
          public RateLimitDecision tryConsume(String redisKey, int capacity, double refillPerSecond) {
              List<Long> raw = redis.execute(SCRIPT, Collections.singletonList(redisKey),
                  List.of(Integer.toString(capacity), Double.toString(refillPerSecond),
                          Long.toString(System.currentTimeMillis())));
              if (raw.get(0) == 1L) {
                  return RateLimitDecision.allowed(raw.get(1));
              }
              return RateLimitDecision.denied((int) raw.get(2));
          }

  - id: "kafka-notification-ingress"
    title: "Kafka notification dispatch ingress"
    description: "When dispatch-mode=kafka, notifications are enqueued to cloud broker instead of processed inline."
    category: "messaging"
    duration: "3 min read"
    views: 0
    tags:
      - "kafka"
      - "notifications"
      - "aws"
    files:
      - name: "KafkaNotificationDispatchIngress.java"
        path: "bank-notifications/src/main/java/io/github/alexistrejo11/bank/notifications/infrastructure/messaging/KafkaNotificationDispatchIngress.java"
        language: "java"
        highlighted: true
        explanation: "Active only when bank.notifications.dispatch-mode=kafka — matches AWS deploy configuration."
        content: |
          @ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "kafka")
          public void submit(DispatchNotificationCommand command) {
              String json = objectMapper.writeValueAsString(NotificationDispatchMessage.from(command));
              kafkaTemplate.send(dispatchTopic, json).whenComplete((r, ex) -> {
                  if (ex != null) {
                      log.warn("notification_dispatch_enqueue_failed topic={}", dispatchTopic, ex);
                  }
              });
          }

  - id: "money-value-object"
    title: "Money value object"
    description: "Immutable currency-aware amount with BigDecimal scale 2 — used everywhere financial math is needed."
    category: "domain"
    duration: "2 min read"
    views: 0
    tags:
      - "shared-kernel"
      - "ddd"
      - "financial"
    files:
      - name: "Money.java"
        path: "bank-shared/src/main/java/io/github/alexistrejo11/bank/shared/shared_kernel/money/Money.java"
        language: "java"
        highlighted: true
        explanation: "Compact constructor rejects negative amounts and normalizes scale — never use double for money."
        content: |
          public record Money(BigDecimal amount, Currency currency) {
              public Money {
                  Objects.requireNonNull(amount);
                  Objects.requireNonNull(currency);
                  if (amount.compareTo(BigDecimal.ZERO) < 0)
                      throw new InvalidMoneyAmountException(amount);
                  amount = amount.setScale(2, RoundingMode.HALF_UP);
              }
          }
---

# Code Showcase

> Snippets are abbreviated from the repository; open the referenced paths for full implementations including tests.

> **Recommended reading order:** ApiResponse envelope → Money value object → transfer idempotency → Kafka notification ingress → Redis rate limiter.

> **AWS context:** Redis and Kafka examples are active when `SPRING_PROFILES_ACTIVE=docker` and corresponding `bank.*.enabled` flags are true — the default local `postgres` profile keeps them off for simpler Maven runs.
