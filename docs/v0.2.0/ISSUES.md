# GitHub Issues — v0.2.0 mapping

Create **one Issue per theme** (or split further if you prefer smaller PRs). Paste the **Implementation ID** from [TRACKER.md](TRACKER.md) into the Issue body.

After creation, record the Issue number in this file and in [TRACKER.md](TRACKER.md).

## Issue table

| Impl ID | Suggested title | Labels (minimum) | Suggested branch |
|---------|-----------------|------------------|------------------|
| V2-DB | v0.2.0: PostgreSQL datasource + Flyway validation | `infra`, `feat`, `version:0.2.0` | `feature/v0.2-postgres-flyway` |
| V2-R | v0.2.0: Enable Redis (IAM tokens + payments idempotency) | `infra`, `feat`, `module:iam`, `module:payments`, `version:0.2.0` | `feature/v0.2-redis` |
| V2-K | v0.2.0: Kafka pipeline for notifications | `infra`, `feat`, `module:notifications`, `version:0.2.0` | `feature/v0.2-kafka-notifications` |
| V2-H | v0.2.0: Security hardening — CORS + rate limits | `feat`, `module:iam`, `version:0.2.0` | `feature/v0.2-http-hardening` |
| V2-O | v0.2.0: JSON logging, audit fields for ELK, `.env.example` | `infra`, `docs`, `version:0.2.0` | `feature/v0.2-observability-secrets` |

## Record Issue numbers (fill after creation)

| Impl ID | GitHub Issue # |
|---------|----------------|
| V2-DB | |
| V2-R | |
| V2-K | |
| V2-H | |
| V2-O | |

## Issue body template

```markdown
## Implementation ID
< V2-DB | V2-R | … >

## Branch
`feature/v0.2-...`

## Scope
(Summary from [ROADMAP.md](ROADMAP.md).)

## Acceptance
- [ ] Tracks in docs/v0.2.0/TRACKER.md updated when work merges
- [ ] PR uses Conventional Commits title and `Closes #<this issue>`
```

## GitHub web paths

Same as [v0.1.0/ISSUES.md](../v0.1.0/ISSUES.md#github-web-paths) — replace `<owner>` and `<repo>`.
