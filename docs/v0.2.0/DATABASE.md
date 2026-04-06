# Database — Flyway, H2 vs PostgreSQL (v0.2.0)

## Where migrations live

All Flyway migrations are owned by **`bank-boot`**: SQL under `src/main/resources/db/migration/` plus Java migrations in `src/main/java/db/migration/` for dialect-specific DDL.

| Version | Kind | Purpose |
|--------|------|---------|
| V5 | Java | `audit_records` with **CLOB** on H2 and **TEXT** on PostgreSQL (matches `@Lob` + `ddl-auto: validate`) |
| V5_1 | Java | Append-only trigger (H2 Java trigger vs PostgreSQL `plpgsql`) |
| V13 | Java | `notifications` lob columns same H2/PG split |

Domain modules no longer ship their own `db/migration` copies; each module sets `spring.jpa.hibernate.ddl-auto: validate` in `application.yml`.

## H2 (tests and default in-memory run)

- **Audit trigger:** Java class `AuditAppendOnlyTrigger` via H2 `CALL` trigger (installed in `V5_1__AuditAppendOnlyTrigger`).
- **Large columns:** Hibernate maps `@Lob` String to **CLOB** on H2; Java migrations use **CLOB** for audit/notifications lob columns so `ddl-auto: validate` succeeds.

## PostgreSQL (profiles `postgres` and `docker`)

- **Audit trigger:** plpgsql function `audit_records_reject_mutation()` plus `BEFORE UPDATE OR DELETE` trigger (same Java migration `V5_1`).
- **Identifiers:** Flyway uses lowercase table/column names consistent with H2 `DATABASE_TO_LOWER=TRUE`.
- **Large columns:** Same Java migrations use **TEXT** for PostgreSQL (aligned with typical `@Lob` String mapping on the PostgreSQL dialect).
- **Optional JSONB:** You can move audit `payload` to `JSONB` in a future migration if you want native JSON operators (document any change in this file).

## Profiles

| Profile    | Datasource        | Notes                                      |
|-----------|-------------------|--------------------------------------------|
| *(default)* | H2 in-memory     | `application.yaml` + optional env overrides |
| `test`    | H2 `banktest`     | `application-test.yaml`                    |
| `postgres`| PostgreSQL        | `application-postgres.yaml` — **`SPRING_DATASOURCE_*` required** (see [CONFIGURATION.md](CONFIGURATION.md)) |
| `docker`  | PostgreSQL + Redis + Kafka | `application-docker.yaml` + env (see [CONFIGURATION.md](CONFIGURATION.md)) |

## CI

Integration tests use the `test` profile (H2 + Flyway). No PostgreSQL container is required for `mvn verify` unless you add separate PG ITs later.
