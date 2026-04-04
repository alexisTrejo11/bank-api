# GitHub Issues — phase mapping

Create **one Issue per row** before starting implementation for that phase. Paste the **Implementation ID** and branch name into the Issue body for traceability.

After creation, record the Issue number in this file and in [TRACKER.md](TRACKER.md).

## Issue table

| Impl ID | Suggested title | Labels (minimum) | Branch |
|---------|-----------------|------------------|--------|
| P0 | Project skeleton: Gradle multi-module, Docker Compose, Flyway baseline, CI, README | `infra`, `feat` (+ optional `phase:0`) | `feature/project-scaffold` |
| P1 | Shared module: value objects, Result, exceptions, events, API envelope | `feat`, `module:shared`, `test` | `feature/shared-module` |
| P2 | IAM: domain, JWT RS256, refresh, blocklist, security, auth REST | `feat`, `module:iam`, `test` | `feature/iam-module` |
| P3 | Accounts: aggregate, ledger, posting, REST, transfer listener stub | `feat`, `module:accounts`, `test` | `feature/accounts-module` |
| P4 | Audit: append-only audit, domain event listener, query API | `feat`, `module:audit`, `test` | `feature/audit-module` |
| P5 | Payments: transfer FSM, Redis idempotency, events to accounts/audit/notifications | `feat`, `module:payments`, `test` | `feature/payments-module` |
| P6 | Loans: aggregate, schedule, origination, repayment | `feat`, `module:loans`, `test` | `feature/loans-module` |
| P7 | Notifications: listeners, templates, console stub, `notification_log` | `feat`, `module:notifications`, `test` | `feature/notifications-module` |
| P8 | Observability and polish: metrics, JSON logs, Grafana, Swagger, seed SQL, release 1.0.0 | `infra`, `docs`, `module:observability`, `test` | `feature/observability` then `release/1.0.0` |

## Record Issue numbers (fill after creation)

| Impl ID | GitHub Issue # |
|---------|----------------|
| P0 | |
| P1 | |
| P2 | |
| P3 | |
| P4 | |
| P5 | |
| P6 | |
| P7 | |
| P8 | |

## Issue body template

Use this in each Issue description:

```markdown
## Implementation ID
< P0 | P1 | … | P8 >

## Branch
`feature/...` or `release/...`

## Scope
(Summary from the title table.)

## Acceptance
- [ ] Tracks in docs/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

## GitHub web paths

- Issues list: `https://github.com/<owner>/<repo>/issues`
- New issue: `https://github.com/<owner>/<repo>/issues/new`
- Labels: `https://github.com/<owner>/<repo>/labels`

Replace `<owner>` and `<repo>` with your repository.
