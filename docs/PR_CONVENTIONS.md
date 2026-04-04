# Pull request conventions

## Title

Use [Conventional Commits](https://www.conventionalcommits.org/) style so history and changelogs stay scannable.

**Format:** `type(scope): description`

**Types:** `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`, etc.

**Examples:**

- `feat(infra): add Gradle multi-project and Docker Compose stack`
- `feat(iam): implement JWT auth with RBAC`
- `feat(accounts): add double-entry posting and REST`
- `fix(payments): correct idempotency key TTL handling`

## Description

Every PR should include:

1. **What** was built or changed (short summary).
2. **Design decisions** that are not obvious from the diff.
3. **How to test** it locally (commands, endpoints, env vars).

## Linked issue

Link the GitHub Issue that tracks this work (create one Issue per phase/feature — see [ISSUES.md](ISSUES.md)).

In the PR description, use:

```text
Closes #<issue-number>
```

Replace `<issue-number>` with the real issue number so merging closes the issue automatically.

## Merge strategy

- **Into `develop`:** prefer **squash merge** for feature branches to keep a linear, readable history on `develop`.
- **Into `main`:** follow your release policy (often squash or merge commit for releases).

## Review (solo)

Even without reviewers, use the PR as a self-review: read the Files changed tab before merging.
