# Documentation index

## Cross-cutting Git (monorepo)

| Document | Purpose |
|----------|---------|
| [MONOREPO_ATOMIC_COMMITS.md](MONOREPO_ATOMIC_COMMITS.md) | One commit per `bank-*` module on `develop`, then cherry-pick onto feature branches (English messages; aligns with GitHub strategy and PR conventions) |

## Process and GitHub

| Document | Purpose |
|----------|---------|
| [v0.1.0/TRACKER.md](v0.1.0/TRACKER.md) | Phase 0–8 implementation status |
| [v0.1.0/ISSUES.md](v0.1.0/ISSUES.md) | Issue ↔ branch mapping |
| [v0.1.0/EXECUTION_WORKFLOW.md](v0.1.0/EXECUTION_WORKFLOW.md) | PR and release steps |
| [v0.1.0/GITHUB_STRATEGY.md](v0.1.0/GITHUB_STRATEGY.md) | Branch rules |
| [v0.1.0/PR_CONVENTIONS.md](v0.1.0/PR_CONVENTIONS.md) | Conventional Commits and PR body |
| [v0.1.0/LABELS.md](v0.1.0/LABELS.md) | GitHub labels |
| [v0.1.0/GITHUB_SETUP_CHECKLIST.md](v0.1.0/GITHUB_SETUP_CHECKLIST.md) | Manual repo setup |

If you batch refactors on `develop` first, split work per module using [MONOREPO_ATOMIC_COMMITS.md](MONOREPO_ATOMIC_COMMITS.md).

## v0.2.0 planning

| Document | Purpose |
|----------|---------|
| [v0.2.0/ROADMAP.md](v0.2.0/ROADMAP.md) | Planned infrastructure and cross-cutting improvements |
| [v0.2.0/TRACKER.md](v0.2.0/TRACKER.md) | Checklist and PR links for 0.2.0 |
| [v0.2.0/ISSUES.md](v0.2.0/ISSUES.md) | Suggested issues ↔ branches |

Agent-facing summaries live under [.agents/](../.agents/) (architecture, domains, conventions).
