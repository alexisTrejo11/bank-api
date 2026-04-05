# Git branching strategy

This document defines how branches are used in the Bank API repository. It applies equally to solo and team workflows.

## Permanent branches

| Branch | Role |
|--------|------|
| `main` | Production-ready code. Receives merges only from `release/*` and `hotfix/*`. Every commit here should correspond to a tagged, deployable version. **Never commit directly.** |
| `develop` | Integration branch. All features merge here first. This is the current work-in-progress line. |

## Temporary branches (delete after merge)

| Pattern | Purpose | Example |
|---------|---------|---------|
| `feature/{scope}-{short-desc}` | One branch per logical chunk of work | `feature/project-scaffold`, `feature/iam-module` |
| `release/{version}` | Cut from `develop` when a module set is ready for release | `release/0.1.0`, `release/1.0.0` |
| `hotfix/{desc}` | Urgent fixes cut from `main` | `hotfix/payments-idempotency-fix` |

## Merge rules

- **`feature/*` → `develop`:** Open a Pull Request. Use **squash merge** to keep `develop` history linear and readable.
- **`release/*` → `main`:** Open a PR to `main`, tag the release on `main` (e.g. `v0.1.0`). Back-merge `main` into `develop` (or merge `release/*` into both per your release process) so `develop` stays aligned.
- **`hotfix/*` → `main`:** Merge via PR, tag if needed. Port the fix to `develop` (merge or cherry-pick) so it is not lost.

## Solo portfolio workflow

Even when working alone, open a **Pull Request** for every feature branch:

- You get a clean diff and a review checkpoint.
- The PR history reads as a professional paper trail for recruiters.

Example flow:

```
develop
  └─► feature/shared-module         (PR → develop, squash)
  └─► feature/iam-module            (PR → develop, squash)
  └─► feature/accounts-module       (PR → develop, squash)
  ...
  └─► release/1.0.0                 (PR → main + tag v1.0.0; back-merge to develop)
```

## GitHub settings (manual)

Configure in the repository **Settings**:

- **Branches → Rulesets** (or branch protection): require a pull request before merging for `main` and `develop`; restrict direct pushes as appropriate for your workflow.

See also [LABELS.md](LABELS.md), [ISSUES.md](ISSUES.md), and [TRACKER.md](TRACKER.md) for labels, issues, and progress.

For **atomic commits per Maven module** and **cherry-picking** onto feature branches (English messages), see [MONOREPO_ATOMIC_COMMITS.md](../MONOREPO_ATOMIC_COMMITS.md).
