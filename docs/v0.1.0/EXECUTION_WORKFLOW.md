# Execution workflow (Phases 1–8)

Use this after Phase 0 ([TRACKER.md](TRACKER.md) Phase 0 complete) is merged to `develop`. Each phase is: **branch → implement → PR → squash to `develop` → update tracker**.

## Repeatable steps (features Phases 1–7)

1. From local `develop` (up to date with `origin/develop`):
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/<name>
   ```
   Use the branch name from [ISSUES.md](ISSUES.md) for that phase.
2. Implement the scope; tick items in [TRACKER.md](TRACKER.md) for that phase as you complete them (or when the PR merges).
3. Push and open a Pull Request **into `develop`**.
4. PR title: Conventional Commits (see [PR_CONVENTIONS.md](PR_CONVENTIONS.md)). Description: what, decisions, how to test. Link issue: `Closes #N`.
5. **Squash merge** into `develop`.
6. Fill **PR URL** and optional **merge SHA** in [TRACKER.md](TRACKER.md) for that phase.
7. Delete the remote feature branch if GitHub offers; delete local branch when done.

## Phase 8 — observability then release

1. Same as above: `feature/observability` → PR → squash → `develop`; update tracker.
2. Cut **`release/1.0.0`** from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b release/1.0.0
   git push -u origin release/1.0.0
   ```
3. Open PR **`release/1.0.0` → `main`**. Merge per your policy (often merge commit or squash for releases).
4. On `main`, tag: `git tag -a v1.0.0 -m "Release 1.0.0"` and `git push origin v1.0.0`.
5. Back-merge: merge `main` into `develop` (or merge `release/1.0.0` into both `main` and `develop`) so `develop` includes the release line.
6. Mark **P8-R** and tag in [TRACKER.md](TRACKER.md).

## Hotfix (optional)

1. Branch from `main`: `hotfix/<short-desc>`.
2. PR to `main`; tag patch if needed.
3. Port to `develop` (merge or cherry-pick).

See [GITHUB_STRATEGY.md](GITHUB_STRATEGY.md) for branch rules.

If you integrated several modules on `develop` first and need **module-pure** feature branches, use [MONOREPO_ATOMIC_COMMITS.md](../MONOREPO_ATOMIC_COMMITS.md) (per-module commits, then cherry-pick onto `feature/*`).
