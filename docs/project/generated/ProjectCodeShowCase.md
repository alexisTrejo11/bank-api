# Code Showcase

## Standardized API response envelope

All REST endpoints return ApiResponse<T> with data, meta.timestamp, and errors[] for logical failures.

**Category:** api | **Duration:** 2 min read | **Tags:** api, shared-kernel, openapi

### ApiResponse.java

**Path:** `bank-shared/src/main/java/io/github/alexistrejo11/bank/shared/shared_kernel/api/ApiResponse.java`

Success and failure factories keep controller responses consistent across modules.

```java
public record ApiResponse<T>(T data, Meta meta, List<ApiError> errors) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, new Meta(Instant.now(), null), List.of());
    }
    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(null, new Meta(Instant.now(), null),
            List.of(new ApiError(code, message, null)));
    }
}
```

## Transfer initiation with idempotency

Payments check Redis cache and DB before processing — duplicate Idempotency-Key returns cached Result.

**Category:** domain | **Duration:** 4 min read | **Tags:** payments, idempotency, redis

### InitiateTransferHandler.java

**Path:** `bank-payments/src/main/java/io/github/alexistrejo11/bank/payments/application/handler/InitiateTransferHandler.java`

Idempotency-Key header is required; self-transfer and currency rules enforced before state transition.

```java
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
```

## Redis token-bucket rate limiter

Atomic Lua script refill + consume in one round trip; fail-open when Redis errors if configured.

**Category:** security | **Duration:** 3 min read | **Tags:** redis, rate-limit, lua

### RedisTokenBucketRateLimiter.java

**Path:** `bank-boot/src/main/java/io/github/alexistrejo11/bank/infrastructure/ratelimit/RedisTokenBucketRateLimiter.java`

Used by GlobalRateLimitFilter and AnnotatedRateLimitInterceptor in docker/AWS profile.

```java
public RateLimitDecision tryConsume(String redisKey, int capacity, double refillPerSecond) {
    List<Long> raw = redis.execute(SCRIPT, Collections.singletonList(redisKey),
        List.of(Integer.toString(capacity), Double.toString(refillPerSecond),
                Long.toString(System.currentTimeMillis())));
    if (raw.get(0) == 1L) {
        return RateLimitDecision.allowed(raw.get(1));
    }
    return RateLimitDecision.denied((int) raw.get(2));
}
```

## Kafka notification dispatch ingress

When dispatch-mode=kafka, notifications are enqueued to cloud broker instead of processed inline.

**Category:** messaging | **Duration:** 3 min read | **Tags:** kafka, notifications, aws

### KafkaNotificationDispatchIngress.java

**Path:** `bank-notifications/src/main/java/io/github/alexistrejo11/bank/notifications/infrastructure/messaging/KafkaNotificationDispatchIngress.java`

Active only when bank.notifications.dispatch-mode=kafka — matches AWS deploy configuration.

```java
@ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "kafka")
public void submit(DispatchNotificationCommand command) {
    String json = objectMapper.writeValueAsString(NotificationDispatchMessage.from(command));
    kafkaTemplate.send(dispatchTopic, json).whenComplete((r, ex) -> {
        if (ex != null) {
            log.warn("notification_dispatch_enqueue_failed topic={}", dispatchTopic, ex);
        }
    });
}
```

## Money value object

Immutable currency-aware amount with BigDecimal scale 2 — used everywhere financial math is needed.

**Category:** domain | **Duration:** 2 min read | **Tags:** shared-kernel, ddd, financial

### Money.java

**Path:** `bank-shared/src/main/java/io/github/alexistrejo11/bank/shared/shared_kernel/money/Money.java`

Compact constructor rejects negative amounts and normalizes scale — never use double for money.

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidMoneyAmountException(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
}
```

## Additional notes

# Code Showcase

> Snippets are abbreviated from the repository; open the referenced paths for full implementations including tests.

> **Recommended reading order:** ApiResponse envelope → Money value object → transfer idempotency → Kafka notification ingress → Redis rate limiter.

> **AWS context:** Redis and Kafka examples are active when `SPRING_PROFILES_ACTIVE=docker` and corresponding `bank.*.enabled` flags are true — the default local `postgres` profile keeps them off for simpler Maven runs.

