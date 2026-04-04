# GitHub setup checklist (manual)

Use this when the repository is on GitHub. If you install [GitHub CLI](https://cli.github.com/) (`gh`), you can automate labels with `gh label create` (see [LABELS.md](LABELS.md)).

## 1. Branch protection

In the repo on GitHub: **Settings → Rules → Rulesets** (or **Branches → Branch protection rules**).

| Branch | Rules |
|--------|--------|
| `main` | Require a pull request before merging; restrict who can push (optional: include administrators for solo) |
| `develop` | Same as above |

Create `develop` from `main` first if it does not exist (see local git commands below).

## 2. Labels

Create each label from [LABELS.md](LABELS.md). Tick when done:

- [ ] `feat` — `#1D76DB`
- [ ] `infra` — `#6A737D`
- [ ] `test` — `#0E8A16`
- [ ] `docs` — `#FBCA04`
- [ ] `module:shared` — `#5319E7`
- [ ] `module:iam` — `#5319E7`
- [ ] `module:accounts` — `#5319E7`
- [ ] `module:audit` — `#5319E7`
- [ ] `module:payments` — `#5319E7`
- [ ] `module:loans` — `#5319E7`
- [ ] `module:notifications` — `#5319E7`
- [ ] `module:observability` — `#5319E7`

Optional phase labels `phase:0` … `phase:8` — see [LABELS.md](LABELS.md).

## 3. Issues (P0–P8)

Create **one issue per row**. Copy the **Title** and **Body** into GitHub **New issue**. Apply **Labels** as listed. After creation, fill in issue numbers in [ISSUES.md](ISSUES.md) and [TRACKER.md](TRACKER.md).

### P0 — Project skeleton

- **Title:** `Project skeleton: Gradle multi-module, Docker Compose, Flyway baseline, CI, README`
- **Labels:** `feat`, `infra` (+ `phase:0` if you use phase labels)

```markdown
## Implementation ID
P0

## Branch
`feature/project-scaffold`

## Scope
Gradle multi-project (root + submodule stub per domain), docker-compose (postgres, redis, prometheus, grafana, elk), root application.yml + env wiring, Flyway `V1__init.sql` baseline, GitHub Actions CI (build + test), README setup instructions.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P1 — Shared module

- **Title:** `Shared module: value objects, Result, exceptions, events, API envelope`
- **Labels:** `feat`, `module:shared`, `test`

```markdown
## Implementation ID
P1

## Branch
`feature/shared-module`

## Scope
Money, AccountId, UserId, LoanId, TransferId; Result<T>; BankException hierarchy; BankDomainEvent; ApiResponse<T>; GlobalExceptionHandler skeleton.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P2 — IAM

- **Title:** `IAM: domain, JWT RS256, refresh, blocklist, security, auth REST`
- **Labels:** `feat`, `module:iam`, `test`

```markdown
## Implementation ID
P2

## Branch
`feature/iam-module`

## Scope
User, Role, Permission; Flyway IAM + seed roles; JWT RS256, refresh, blocklist; Spring Security; /auth/register, /login, /refresh, /logout; @PreAuthorize; unit + integration tests.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P3 — Accounts

- **Title:** `Accounts: aggregate, ledger, posting, REST, transfer listener stub`
- **Labels:** `feat`, `module:accounts`, `test`

```markdown
## Implementation ID
P3

## Branch
`feature/accounts-module`

## Scope
Account aggregate + LedgerEntry; migrations; double-entry posting; REST; AccountsTransferListener stub; tests.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P4 — Audit

- **Title:** `Audit: append-only audit, domain event listener, query API`
- **Labels:** `feat`, `module:audit`, `test`

```markdown
## Implementation ID
P4

## Branch
`feature/audit-module`

## Scope
AuditRecord; append-only trigger migration; AuditEventListener for BankDomainEvent; filtered query endpoint; tests.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P5 — Payments

- **Title:** `Payments: transfer FSM, Redis idempotency, events to accounts/audit/notifications`
- **Labels:** `feat`, `module:payments`, `test`

```markdown
## Implementation ID
P5

## Branch
`feature/payments-module`

## Scope
Transfer aggregate + state machine; Redis idempotency; execution + events; TransferCompletedEvent wired to accounts, audit, notifications; tests.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P6 — Loans

- **Title:** `Loans: aggregate, schedule, origination, repayment`
- **Labels:** `feat`, `module:loans`, `test`

```markdown
## Implementation ID
P6

## Branch
`feature/loans-module`

## Scope
Loan aggregate + repayment schedule; origination + disbursement event; repayment endpoint; tests.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P7 — Notifications

- **Title:** `Notifications: listeners, templates, console stub, notification_log`
- **Labels:** `feat`, `module:notifications`, `test`

```markdown
## Implementation ID
P7

## Branch
`feature/notifications-module`

## Scope
Event listeners; template engine + console stub; notification_log persistence; tests.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

### P8 — Observability and release

- **Title:** `Observability and polish: metrics, JSON logs, Grafana, Swagger, seed SQL, release 1.0.0`
- **Labels:** `infra`, `docs`, `module:observability`, `test`

```markdown
## Implementation ID
P8

## Branch
`feature/observability` then `release/1.0.0`

## Scope
Micrometer per module; Logback JSON + MDC; Grafana provisioning; Swagger + R__seed_demo_data.sql; release branch merge to main; tag v1.0.0.

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

## 4. Local git: `develop` and first feature branch

After committing documentation, on your machine:

```bash
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop
git checkout -b feature/project-scaffold
# … implement Phase 0, then push and open PR develop ← feature/project-scaffold
```

Repository URL: `https://github.com/alexisTrejo11/bank-api`
