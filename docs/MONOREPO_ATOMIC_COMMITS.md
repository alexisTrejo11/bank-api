# Atomic commits per module (monorepo)

This workflow keeps **one logical change set per Maven module** when you integrate on `develop` (or a long-lived fix branch). It pairs with [GITHUB_STRATEGY.md](v0.1.0/GITHUB_STRATEGY.md), [PR_CONVENTIONS.md](v0.1.0/PR_CONVENTIONS.md), and [EXECUTION_WORKFLOW.md](v0.1.0/EXECUTION_WORKFLOW.md).

## Why

- **History:** Each commit maps to a single `bank-*` module (plus shared root files only when needed).
- **Cherry-picks:** You can move only the accounts work onto `feature/accounts-module` without dragging loans, payments, or audit changes.
- **Reviews:** PRs scoped to one module stay small and easy to read.

## Rules (aligns with repo conventions)

- **Commit messages:** [Conventional Commits](https://www.conventionalcommits.org/) in **English**, scope = module name (see [.agents/conventions.md](../.agents/conventions.md) § Git conventions).
- **Branches:** `feature/{scope}-{short-desc}` per [GITHUB_STRATEGY.md](v0.1.0/GITHUB_STRATEGY.md).
- **Parent `pom.xml`:** Include the **root** `pom.xml` in a commit only when that commit actually changes the parent (dependency management, modules list, plugin config). Do not bundle unrelated root changes into a module-only commit.

## Step 1 — Atomic commits on `develop`

Work on `develop` (or your integration branch), staging **only** paths that belong to one module per commit.

```bash
git checkout develop
git pull origin develop
```

**Example — accounts only:**

```bash
git add bank-accounts/
git add pom.xml   # only if this commit changes the parent POM
git commit -m "refactor(accounts): apply hexagonal layout and handlers"
```

**Example — loans only:**

```bash
git add bank-loans/
git commit -m "refactor(loans): apply hexagonal layout and handlers"
```

Repeat for `bank-payments`, `bank-audit`, `bank-notifications`, `bank-boot`, `bank-shared`, and so on. Cross-cutting docs (for example `.agents/` or `docs/`) get their own commit when they stand alone: `docs: add monorepo atomic commit guide`.

## Step 2 — Bring changes onto the matching feature branch

Use **cherry-pick** to apply exactly the commit(s) for that module. Branch names should match [ISSUES.md](v0.1.0/ISSUES.md) / [TRACKER.md](v0.1.0/TRACKER.md) (for example `feature/accounts-module`).

1. **Identify the commit** (English message, clear scope):

   ```bash
   git log develop --oneline
   ```

2. **Check out the feature branch** (create or update from `origin/develop` as needed):

   ```bash
   git fetch origin
   git checkout feature/accounts-module
   # or: git checkout -b feature/accounts-module origin/develop
   ```

3. **Cherry-pick** the accounts commit:

   ```bash
   git cherry-pick <commit-sha>
   ```

4. **Resolve conflicts** if the feature branch diverged; then continue:

   ```bash
   git cherry-pick --continue
   ```

5. **Push** and open a PR **into `develop`** per [EXECUTION_WORKFLOW.md](v0.1.0/EXECUTION_WORKFLOW.md).

Repeat on `feature/loans-module`, `feature/payments-module`, etc., each with only the SHA(s) for that module.

**Result:** The feature branch contains **no** unrelated module trees from other commits, so the PR diff stays module-pure.

## Checks before push

- **Build/tests:** From repo root, run Maven for the affected module(s), for example:
  - `mvn -pl bank-accounts -am verify`
  - or full `mvn verify` if you touched shared parents or multiple modules in one PR after cherry-picks.
- **Diff review:** `git diff develop...HEAD` (or the PR diff on GitHub) should only show paths under the intended module(s) plus any intentional shared files.
- **Message language:** Commit and PR titles stay **English**, per [PR_CONVENTIONS.md](v0.1.0/PR_CONVENTIONS.md).

## When not to use this

- **Single-module PR from the start:** If you already implemented on `feature/accounts-module` only, merge normally; no cherry-pick needed.
- **Tightly coupled change:** If two modules must land together (shared API + both consumers), one commit or one PR spanning those modules is acceptable; document why in the PR body.

## See also

- [GITHUB_STRATEGY.md](v0.1.0/GITHUB_STRATEGY.md) — branch types and merge rules  
- [EXECUTION_WORKFLOW.md](v0.1.0/EXECUTION_WORKFLOW.md) — PR → squash → `develop`  
- [.agents/conventions.md](../.agents/conventions.md) — commit format and scopes  
