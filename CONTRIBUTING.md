# Contributing to Simplicity

## PR-flow discipline (portfolio certification standard)

- Direct pushes to `main` are retired. All changes land via **draft pull request**.
- Draft PR → tests green → owner (Nrupal Akolkar) merges. No one else merges.
- Every PR adds a CHANGELOG entry under `## [Unreleased]` (Keep a Changelog)
  and bumps the version per SemVer: **patch** = fix/chore, **minor** = feature,
  **major** = breaking change.
- Merge commits reference the PR number; releases are tagged `vX.Y.Z` after merge.
- The `version-bump.yml` workflow auto-bumps on merge to `main` and validates
  CHANGELOG presence plus parent/module POM version consistency.

## Build and test

### Java backend (requires Java 21; Maven wrapper is committed)

```bash
./mvnw clean compile -DskipTests -B   # compile all modules
./mvnw package -DskipTests -B          # package jars
```

(Commands mirror `.github/workflows/java-build.yml`, which also copies built
jars to `release/` and uploads them as a workflow artifact.)

### Web PWA (requires Node.js 20)

```bash
cd simplicity-web
npm install
npm run build    # vite production build -> simplicity-web/dist/
npm test         # vitest suite
npm run lint     # eslint
npm run dev      # vite dev server on :3000
```

(Scripts taken from `simplicity-web/package.json`; not executed in this change.)

### Android

```bash
cd simplicity-android
./gradlew assembleDebug   # standard Gradle build
```

## Release

Releases are produced by `.github/workflows/release.yml` on tag push (`v*.*.*`)
plus manual dispatch: the PWA is built, then a GitHub Release is created with
the `pwa/` artifacts. See the workflow file for the exact steps.
