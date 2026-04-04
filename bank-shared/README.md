# bank-shared

Reusable kernel for the Bank API monolith: value objects, `Result`, exceptions, domain event base type, HTTP envelope, and global RFC 7807 error handling (`io.github.alexistrejo11.bank.shared`).

Other domain modules (IAM, accounts, …) should depend on **`bank-shared` only** for these types—not on `bank-boot`.

## Use from another Maven module (same repo)

```xml
<dependency>
  <groupId>io.github.alexistrejo11</groupId>
  <artifactId>bank-shared</artifactId>
  <version>${project.version}</version>
</dependency>
```

Align `version` with the parent `bank-parent` (or import a BOM once you publish one).

## Install into the local repository

From the **repository root**:

```bash
./mvnw -pl bank-shared install
```

Then reference `io.github.alexistrejo11:bank-shared:0.0.1-SNAPSHOT` from another Spring project on your machine.

## Build and test only this module

```bash
./mvnw -pl bank-shared verify
```
