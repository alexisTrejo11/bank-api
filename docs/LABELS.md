# GitHub labels

Create these labels in the repository (**Issues → Labels**). Colors are suggestions for consistency on the project board.

| Label | Color (hex) | Use |
|-------|----------------|-----|
| `feat` | `#1D76DB` | New feature |
| `infra` | `#6A737D` | Config, Docker, CI |
| `test` | `#0E8A16` | Test coverage |
| `docs` | `#FBCA04` | README, OpenAPI |
| `module:shared` | `#5319E7` | Shared kernel |
| `module:iam` | `#5319E7` | IAM |
| `module:accounts` | `#5319E7` | Accounts |
| `module:audit` | `#5319E7` | Audit |
| `module:payments` | `#5319E7` | Payments |
| `module:loans` | `#5319E7` | Loans |
| `module:notifications` | `#5319E7` | Notifications |
| `module:observability` | `#5319E7` | Metrics, logs, dashboards |

## Optional phase labels

For board columns by phase:

| Label | Color (hex) | Use |
|-------|----------------|-----|
| `phase:0` | `#D4C5F9` | Phase 0 — project skeleton |
| `phase:1` | `#D4C5F9` | Phase 1 — shared module |
| `phase:2` | `#D4C5F9` | Phase 2 — IAM |
| `phase:3` | `#D4C5F9` | Phase 3 — accounts |
| `phase:4` | `#D4C5F9` | Phase 4 — audit |
| `phase:5` | `#D4C5F9` | Phase 5 — payments |
| `phase:6` | `#D4C5F9` | Phase 6 — loans |
| `phase:7` | `#D4C5F9` | Phase 7 — notifications |
| `phase:8` | `#D4C5F9` | Phase 8 — observability and release |

## `gh` CLI (optional)

If you use GitHub CLI, you can create labels from this file with commands like:

```bash
gh label create "feat" --color "1D76DB" --description "New feature"
```

Repeat for each row, adjusting name and color as needed.
