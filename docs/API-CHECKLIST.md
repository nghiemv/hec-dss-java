# Public API Review Checklist

Use this before merging any PR that touches `src/main/java/mil/army/usace/hec/dss/` (excluding `.internal/`).

## Automated gates (CI)

These are enforced by `./gradlew check`. If CI is green, all of these passed:

- [ ] **`compileJava`** — source compiles.
- [ ] **`test`** — unit/integration tests pass.
- [ ] **`javadoc`** — strict `-Xdoclint:all` passes. Catches stale `@link`, malformed HTML, missing params, broken cross-references. See [build.gradle.kts](../build.gradle.kts).
- [ ] **`checkApiSnapshot`** — the committed [`api/public-api.txt`](../api/public-api.txt) matches the current compiled API. Any diff is visible in the PR.
- [ ] **`compileClientTestJava`** — [`src/clientTest/java/`](../src/clientTest/java) compiles as a separate module that only sees exported packages. Catches `.internal` leaks and broken client call sites.

## Manual review

### Every PR

- [ ] **Read the [`api/public-api.txt`](../api/public-api.txt) diff first.** Every addition, removal, and signature change is one line. Ask: is each change intentional, named correctly, and documented?
- [ ] **Read the [`docs/API-DESIGN.md`](API-DESIGN.md) rules.** Does the change follow them? If it violates one, is there a compelling reason written in the PR description?

### New public method

- [ ] Named with the right prefix: `read*` / `write*` for record payloads, `get*` for file-level metadata.
- [ ] All parameters are either documented primitives/records or public types from this package or `java.*`. **No `.internal` types in the signature.**
- [ ] `@throws DssException` (if it can fail at runtime) is in the javadoc.
- [ ] Has a one-line purpose statement in javadoc.
- [ ] Exercised by a call in [`ClientUsage.java`](../src/clientTest/java/clienttest/ClientUsage.java).

### New public class / record

- [ ] Class-level javadoc with a one-line purpose and at least one runnable-looking `{@code}` example.
- [ ] Value object pattern: private constructor + `static of(...)` factories (records may keep the canonical ctor public).
- [ ] Array accessors follow `value(int)` / `values()` shape; counts end in `*Count()`; missing values are `Double.NaN`.
- [ ] No `get*` prefix on accessors.
- [ ] Exercised in [`ClientUsage.java`](../src/clientTest/java/clienttest/ClientUsage.java).

### Breaking changes

- [ ] Listed in [`CHANGELOG.md`](../CHANGELOG.md) under the current version's `### Breaking` heading.
- [ ] Migration example shown (one line of before / after).
- [ ] All internal callers updated.
- [ ] All tests updated.

### Internal escape hatches

- [ ] If you had to expose something to `.internal` readers/writers, it's inside a nested `Internal` class on the value object — **not** as a top-level public method.
- [ ] The nested class is marked `@hidden` in javadoc.
- [ ] The nested class's signatures reference `.internal` types (so JPMS hides them from clients).

## Red flags that stop the review

- A method ending in `fromNative`, `toNative`, or `_impl` on the public surface.
- A `@deprecated` without a `@since` and replacement.
- A public mutable field.
- A public constructor on a final value object class that isn't a record.
- An `Optional.empty()` returned from a method that used to return a concrete value (breaks clients silently).
- Javadoc that references a method, class, or package that doesn't exist (doclint should catch this, but verify anyway).

## Shipping the release

- [ ] `./gradlew build` from a clean tree.
- [ ] Regenerated javadoc published somewhere reviewers can click through.
- [ ] `api/public-api.txt` committed alongside the release commit.
- [ ] Version bumped in [`build.gradle.kts`](../build.gradle.kts) and `CHANGELOG.md`.
