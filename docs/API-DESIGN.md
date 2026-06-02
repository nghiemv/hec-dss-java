# hec-dss-java API Design

This document codifies the design decisions behind the `mil.army.usace.hec.dss` public package. Read it before adding or modifying public API surface.

## Goals

- **Client-first.** The client's three-line happy-path snippet drives the design. Resource management, native lifecycle, error inspection, and record-type dispatch are all hidden.
- **Obvious.** Method names and shapes should be guessable. A Java developer should write the right call on the first try without reading javadoc.
- **Boring.** Prefer well-known Java idioms over cleverness. Static factories, plain records, defensive copies, unchecked exceptions.

## Core patterns

### `HecDss` is the only entry point

All file operations are static methods on `HecDss`. No `HecDss` instances, no try-with-resources at the call site, no session objects visible to clients. Each method opens and closes its own native handle internally.

```java
var ts = HecDss.readTimeSeries(file, pathname);
```

**Why:** DSS clients typically read one record at a time and hold the resulting value object for a long time. Reads are rare compared to I/O cost, so per-call open/close is fine — and it spares the client the lifecycle burden.

### `read*` / `write*` / `get*` — three prefixes, clear split

- `read*` / `write*` operate on **record payloads** (time series, grids, paired data, arrays, text, location info).
- `get*` returns **file-level metadata** (catalog, record count, record type).
- Bare verbs (`deleteRecord`, `squeeze`) are for file operations that don't fit either category.

Never cross the streams. A method named `getTimeSeries` is wrong — that's a read. A method named `readRecordCount` is wrong — that's metadata.

### Plain value objects, not wrappers

Every record type has a corresponding value object: `DssTimeSeries`, `DssGrid`, `DssPairedData`, `DssLocationInfo`. These are immutable, hold only Java arrays (no native pointers), and survive the enclosing `HecDss.read*` call indefinitely. Clients can pass them between threads, serialize them, cache them — anything.

### One factory path, private constructors

Value objects expose `static of(...)` factories and keep their constructors private. This gives one way to create an instance, discoverable in one place, with room to change internal layout later without a breaking change.

Records (`DssLocationInfo`, `DssCatalogEntry`, `DssPathname`) are an exception — their canonical constructors are part of the record contract, so they stay public. But add an `of(...)` factory for the common case when the full ctor is awkward.

### Defensive copies for array accessors

`values()`, `times()`, `ordinates()`, `quality()`, etc. all return defensive copies. Clients can't accidentally mutate a value object. Indexed accessors (`value(int)`, `time(int)`) return by value and skip the copy cost.

### Singular vs. plural accessor convention

For any array-backed field, provide both shapes:

| Shape | Meaning | Example |
|---|---|---|
| `noun(int index)` | single element at that index | `value(0)`, `time(0)`, `curve(0)` |
| `nouns()` | defensive copy of the whole array | `values()`, `times()`, `curves()` |

Exception: `quality` is a mass noun, so it stays `quality(int)` / `quality()` rather than `qualities()`.

### Count accessors end in `Count`

`size()` is reserved for the primary dimension (time series length, grid cell count). Secondary counts use `<noun>Count()`:

- `DssPairedData.ordinateCount()`, `curveCount()`
- Not `numberOrdinates()`, `numberCurves()` — that phrasing reads awkwardly in Java.

### Missing values are `Double.NaN`

Every numeric record type represents missing values as `Double.NaN`. Use `isMissing(int)` to test and `dropMissing()` to filter. The vocabulary is **"missing"** everywhere — never "undefined", "NA", "null", or "sentinel".

## Exceptions

One unchecked exception type: `DssException` (extends `RuntimeException`).

```java
try {
    var ts = HecDss.readTimeSeries(file, pathname);
} catch (DssException e) {
    log.error("read failed", e);
}
```

- The message describes exactly what went wrong. Clients never need to inspect fields or subtypes.
- No subtypes yet. Add them only if a real use case emerges where clients must programmatically distinguish failure modes.
- `NullPointerException` is thrown for null arguments. `IllegalArgumentException` is thrown for invalid-but-non-null inputs that are the client's fault (e.g. mismatched array lengths on construction).
- `DssException` is for everything else: missing files, invalid pathnames, native failures, type mismatches.

## Internal escape hatches

Some record types need to round-trip DSS-native metadata that clients have no business touching (e.g. `DssGrid`'s native range histogram). The pattern for this is a nested `Internal` class:

```java
public final class DssGrid {
    // ... normal client API ...

    /** @hidden */
    public static final class Internal {
        public static DssGrid create(..., NativeGridMetadata meta) { ... }
        public static NativeGridMetadata metadataOf(DssGrid g) { ... }
    }
}
```

- The `Internal` nested class holds factory + accessor methods used only by `.internal` readers/writers.
- Its signatures reference types in `mil.army.usace.hec.dss.internal`, which is **not exported** in [module-info.java](../src/main/java/module-info.java). Strict modular clients physically cannot call these methods.
- Non-modular clients may see `DssGrid.Internal` in autocomplete but cannot use the return values (the types aren't on their classpath).
- Never put backdoor methods directly on the value object (`fromNative(...)`, `nativeMetadata()` etc.). Always wrap them in a nested `Internal` class.

## Adding a new record type

When adding support for a new DSS record type (TIN, paired data variants, etc.), follow this template:

1. **Value object** in `mil.army.usace.hec.dss`: plain record or final class, private ctor, `static of(...)` factory, array accessors with `value(i)`/`values()` shape, defensive copies.
2. **Reader + writer** in `mil.army.usace.hec.dss.internal`: package-private helpers that wrap the FFM calls.
3. **Two static methods on `HecDss`**: `readFoo(Path file, String pathname)` and `writeFoo(Path file, String pathname, DssFoo data)`. Use `expectRecordType(...)` in the reader.
4. **Add a usage sample** in `src/clientTest/java/clienttest/ClientUsage.java` that exercises both the read and write paths plus every accessor.
5. **Regenerate the API snapshot**: `./gradlew apiSnapshot`, commit `api/public-api.txt`.

## What *not* to do

- ❌ Instance methods on `HecDss` — it's a static utility class.
- ❌ Checked exceptions — all DSS errors are `DssException`.
- ❌ Overloads for minor parameter differences — prefer one method with clearer name.
- ❌ `getXxx()` prefix on value object accessors — we follow record-style `xxx()`.
- ❌ Public constructors on value objects (records exempted).
- ❌ Exposing `.internal` types in any public signature.
- ❌ Breaking the singular/plural accessor convention.
