# Test plan — Simplicity

## Scope

Simplicity is a user-owned, immutable, portable knowledge graph: a Java 21
Maven multi-module backend (`simplicity-core`, `-rag`, `-knowledge-graph`,
`-model-registry`, `-intelligence`, `-sovereignty`, `-model-binder`, `-api`)
plus the `simplicity-web` front end (Vite + Vitest).

Stated exactly, because the plan must match the repo: **the Java modules have
no automated test suite today** — the Java gate is compilation and packaging.
The web app has a Vitest suite. Writing the Java suite is tracked product
work, not something this plan pretends exists.

## Suites

| Suite | Covers | Run it | Runs in CI |
|---|---|---|---|
| Maven build gate | all 8 Java modules compile and package (Java 21, Temurin) | `./mvnw clean compile -DskipTests -B` then `./mvnw package -DskipTests -B` | Yes — Java Build |
| Web tests (Vitest) | the `simplicity-web` front end | `npm test` in `simplicity-web/` | Not yet — no workflow runs it (named gap) |
| Version consistency | parent POM version matches module POMs; CHANGELOG present | the Version Bump workflow's check jobs | Yes — Version Bump |
| Static analysis (CodeQL) | the Java codebase | GitHub code scanning | Yes — CodeQL workflow |

## CI gates

- `Java Build` (`.github/workflows/java-build.yml`) compiles and packages
  every module on every push to `main`; it must be green before a release.
  **Current state (2026-11-06): red** — `simplicity-core` does not compile
  (`UserCustomizationService` is written against an extended
  `UserCustomization` shape the domain records do not declare). Fixing that
  mismatch is product work for the maintainer; this plan records it, it does
  not hide it.
- CodeQL (`.github/workflows/codeql.yml`) runs on push and pull request to
  `main` and weekly; findings are dispositioned on the house SLA (Critical
  48 h, High 7 d, Medium 14 d — fix, or dismiss with the reason recorded).

## Cadence

- Every push to `main`: Java Build + the Version Bump check jobs.
- Every pull request: CodeQL.
- Weekly: the scheduled CodeQL scan.
- Every release candidate: the release-readiness run (checker + evidence
  reads), per the house Release-Readiness Standard.

## Coverage bar

No numeric floor exists today. The house rule going forward, per
CONTRIBUTING: new functionality lands with tests, and the first Java suite
should start with `simplicity-core`, whose domain records the rest of the
system builds on.
