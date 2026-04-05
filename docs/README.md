# Documentation index

## Cross-cutting Git (monorepo)

| Document | Purpose |
|----------|---------|
| [MONOREPO_ATOMIC_COMMITS.md](MONOREPO_ATOMIC_COMMITS.md) | One commit per `bank-*` module on `develop`, then cherry-pick onto feature branches (English messages; aligns with GitHub strategy and PR conventions) |

## Process and GitHub

| Document | Purpose |
|----------|---------|
| [TRACKER.md](TRACKER.md) | Phase 0–8 implementation status |
| [ISSUES.md](ISSUES.md) | Issue ↔ branch mapping |
| [EXECUTION_WORKFLOW.md](EXECUTION_WORKFLOW.md) | PR and release steps |
| [GITHUB_STRATEGY.md](GITHUB_STRATEGY.md) | Branch rules |
| [PR_CONVENTIONS.md](PR_CONVENTIONS.md) | Conventional Commits and PR body |
| [LABELS.md](LABELS.md) | GitHub labels |
| [GITHUB_SETUP_CHECKLIST.md](GITHUB_SETUP_CHECKLIST.md) | Manual repo setup |

If you batch refactors on `develop` first, split work per module using [MONOREPO_ATOMIC_COMMITS.md](MONOREPO_ATOMIC_COMMITS.md).

Agent-facing summaries live under [.agents/](../.agents/) (architecture, domains, conventions).
