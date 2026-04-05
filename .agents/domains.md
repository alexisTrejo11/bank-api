# DOMAINS.md

## Shared value objects (`io.github.alexistrejo11.bank.shared`)

These are used across all modules. Never re-define them locally.

```java
// Monetary value — immutable, currency-aware
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidMoneyAmountException(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    public Money add(Money other) { assertSameCurrency(other); return new Money(amount.add(other.amount), currency); }
    public Money subtract(Money other) { assertSameCurrency(other); return new Money(amount.subtract(other.amount), currency); }
    public boolean isPositive() { return amount.compareTo(BigDecimal.ZERO) > 0; }
}

public record AccountId(UUID value) { public static AccountId of(UUID v) { return new AccountId(v); } }
public record UserId(UUID value)    { public static UserId of(UUID v)    { return new UserId(v);    } }
public record LoanId(UUID value)    { public static LoanId of(UUID v)    { return new LoanId(v);    } }
public record TransferId(UUID value){ public static TransferId of(UUID v){ return new TransferId(v);} }
```

## Module: IAM (`io.github.alexistrejo11.bank.iam`)

### Aggregates
- `User` — root aggregate. Has `UserId`, `email`, `passwordHash`, `UserStatus`, `Set<Role>`.
- `Role` — `name`, `Set<Permission>`.
- `Permission` — fine-grained string: `accounts:read`, `accounts:write`, `payments:write`, `loans:read`, `loans:write`, `audit:read`, `admin:all`.

### Business rules
1. Email must be unique across all users.
2. Password stored as Bcrypt hash (cost factor ≥ 12). Never log or return in any response.
3. JWT issued with RS256 (asymmetric). Payload includes `roles[]` and `permissions[]`.
4. Access token TTL = 15 minutes. Refresh token TTL = 7 days (stored in Redis).
5. On logout, add JWT `jti` to Redis blocklist with remaining TTL.
6. On refresh, rotate the refresh token — old token is invalidated immediately.
7. `UserStatus` values: `ACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`.
8. A `SUSPENDED` user's JWT is rejected even before expiry (check status on each request or embed in JWT and blocklist on suspend).

### Default seed roles
| Role | Permissions |
|---|---|
| `CUSTOMER` | `accounts:read`, `accounts:write`, `payments:write`, `loans:read`, `loans:write` |
| `ADMIN` | `admin:all` |
| `AUDITOR` | `accounts:read`, `audit:read`, `loans:read` |

---

## Module: Accounts (`io.github.alexistrejo11.bank.accounts`)

### Aggregates
- `Account` — root. Has `AccountId`, `UserId`, `AccountType`, `Currency`, `AccountStatus`.
- `LedgerEntry` — child entity. Has `LedgerEntryId`, `AccountId`, `EntryType (DEBIT|CREDIT)`, `Money`, `referenceId`, `referenceType`, `createdAt`.

### Business rules
1. **Balance is always derived from the ledger — never stored.** `balance = SUM(CREDIT) - SUM(DEBIT)` for the account.
2. Every balance mutation (deposit, withdrawal, transfer, loan disbursement, repayment) produces exactly two `LedgerEntry` rows: one DEBIT and one CREDIT on the affected accounts.
3. `AccountType` values: `CHECKING`, `SAVINGS`, `LOAN`.
4. `AccountStatus` values: `ACTIVE`, `FROZEN`, `CLOSED`.
5. A `FROZEN` or `CLOSED` account rejects all mutations — return `Result.failure("ACCOUNT_NOT_OPERABLE", ...)`.
6. Currency is set at account creation and is immutable.

### Events consumed
| Event | Action |
|---|---|
| `TransferCompletedEvent` | Post DEBIT on source account, CREDIT on target account |
| `LoanDisbursedEvent` | Post CREDIT on customer checking account |
| `LoanRepaymentCompletedEvent` | Post DEBIT on customer checking account |

---

## Module: Payments (`io.github.alexistrejo11.bank.payments`)

### Aggregates
- `Transfer` — root. Has `TransferId`, source `AccountId`, target `AccountId`, `Money`, `TransferStatus`, `idempotencyKey`, timestamps.

### State machine
```
PENDING → PROCESSING → COMPLETED
                    ↘ FAILED
          COMPLETED → REVERSED
```
State transitions are enforced inside the `Transfer` aggregate — no direct field mutation from outside.

### Business rules
1. Every POST `/transfers` requires `Idempotency-Key` UUID header.
2. Idempotency check: look up `idempotencyKey` in Redis (TTL = 24h). If found, return the cached response — do not re-process.
3. Source and target accounts must be in the same currency (no FX in v1 — return `Result.failure("CURRENCY_MISMATCH", ...)`).
4. Source account must have sufficient balance before transitioning to `PROCESSING`.
5. Transfer amount must be > 0.
6. A transfer cannot be self-referential (source ≠ target).
7. Only `COMPLETED` transfers may be reversed. Reversal creates a new `Transfer` with `referenceTransferId` pointing to the original.

### Events published
| Event | When |
|---|---|
| `TransferCompletedEvent` | After successful ledger posting |
| `TransferFailedEvent` | After any failure during processing |
| `TransferReversedEvent` | After reversal transfer completes |

---

## Module: Loans (`io.github.alexistrejo11.bank.loans`)

### Aggregates
- `Loan` — root. Has `LoanId`, `AccountId` (associated checking account), `Money` (principal), `interestRate`, `termMonths`, `LoanStatus`, `List<LoanRepayment>`.
- `LoanRepayment` — child. Has `LoanRepaymentId`, due date, `Money` (amount), `RepaymentStatus`, `paidAt`.

### Business rules
1. Amortization schedule is generated at origination using fixed monthly payment formula:
   `M = P * [r(1+r)^n] / [(1+r)^n - 1]`  where r = monthly rate, n = term in months.
2. `LoanStatus` values: `PENDING_APPROVAL`, `ACTIVE`, `PAID_OFF`, `DEFAULTED`.
3. `RepaymentStatus` values: `PENDING`, `PAID`, `OVERDUE`.
4. A loan account (`AccountType.LOAN`) is created automatically on loan approval.
5. Repayment debits the customer's checking account via `LoanRepaymentCompletedEvent`.
6. When all repayments reach `PAID` status, loan transitions to `PAID_OFF` automatically.
7. Paying off an already-paid installment returns `Result.failure("REPAYMENT_ALREADY_PAID", ...)`.
8. Interest rate is stored as BigDecimal (e.g. `0.0125` = 1.25% monthly). Never use double/float for financial math.

### Events published
| Event | When |
|---|---|
| `LoanApprovedEvent` | After origination approval |
| `LoanDisbursedEvent` | After funds are credited to customer account |
| `LoanRepaymentCompletedEvent` | After each installment is paid |
| `LoanPaidOffEvent` | When all installments reach PAID status |

---

## Module: Notifications (`io.github.alexistrejo11.bank.notifications`)

### Business rules
1. Notifications are fire-and-forget — failures do not roll back the originating transaction.
2. Every notification is logged in `notification_log` with status `SENT` or `FAILED`.
3. Templates are keyed by `templateKey` string (e.g. `transfer.completed`, `loan.approved`).
4. In local/dev environment, notifications are logged to console only — no real SMTP.
5. In **v0.1.0**, dispatch is triggered by an `ApplicationEvent` listener, not by calling SMTP/template code from other modules directly.
6. In **v0.2.0**, the same business triggers may arrive via **Kafka** (consumer in `bank-notifications`); the domain still does not depend on broker APIs—only infrastructure adapters do.

### Events consumed
| Event | Notification sent |
|---|---|
| `TransferCompletedEvent` | Email to sender (debit) and receiver (credit) |
| `TransferFailedEvent` | Email to sender |
| `LoanApprovedEvent` | Email to customer |
| `LoanRepaymentCompletedEvent` | Email receipt to customer |
| `LoanPaidOffEvent` | Congratulations email to customer |

---

## Module: Audit (`io.github.alexistrejo11.bank.audit`)

### Aggregates
- `AuditRecord` — append-only. Has `id`, `eventType`, `actorId` (UserId), `entityType`, `entityId`, `payload` (JSONB), `createdAt`.

### Business rules
1. `AuditRecord` is NEVER updated or deleted. Enforced by a DB-level trigger (`BEFORE UPDATE OR DELETE → RAISE EXCEPTION`).
2. Every domain event from every module is captured. The `AuditEventListener` subscribes to all events that extend `BankDomainEvent`.
3. `payload` stores the full event serialized as JSON — provides full reconstruction capability.
4. Queries support filtering by `eventType`, `actorId`, `entityType`, `entityId`, date range.
5. Only users with `audit:read` permission may query audit records.
6. `AuditRecord` IDs use UUID v7 (time-ordered) to allow efficient range queries by creation time.

---

## Financial math rules (apply everywhere)
- Always use `BigDecimal` for monetary values. Never `double` or `float`.
- Always use `RoundingMode.HALF_UP` for rounding.
- Always store amounts with scale 2 (cents precision).
- Currency stored as ISO 4217 code string (e.g. `"MXN"`, `"USD"`).
- All timestamps stored as `TIMESTAMPTZ` (UTC) in DB; exposed as `Instant` in Java.