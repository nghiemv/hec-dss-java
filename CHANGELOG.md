# Changelog

## 0.0.4 — Working macOS native (x86_64)

- **macOS native now loads.** Bundles the `darwin-x86_64` `libhecdss.dylib`
  (Apple Silicon runs it under Rosetta). The upstream dylib resolves zlib via
  `@rpath/libz.1.dylib` but ships only a build-machine `LC_RPATH`, so the build
  rewrites that rpath in place to `/usr/lib` — otherwise dyld cannot find zlib
  and the load fails (the cause of the non-loading `0.0.3` macOS native).
  arm64 is deferred: its dylib carries a code signature an in-place patch would
  invalidate. CI now builds and tests on macOS alongside Linux/Windows.

## 0.0.3 — Native 7-JA-7, bundled header

- **HEC-DSS natives bumped to 7-JA-7** (from 7-JA-6). The bundled `hecdss.h`
  is byte-identical to the previously pinned header, so the committed FFM
  bindings are unchanged and ABI-compatible.
- **Bindings generated from the bundled header.** `7-JA-7+` native zips ship
  `hecdss.h` alongside the library, so `generateBindings` now extracts the
  header from the zip instead of downloading a separately pinned commit from
  GitHub — the header can no longer drift from the binary. The header is
  excluded from the published jar.
- **macOS wiring (non-loading).** Added `osx_64` / `osx_arm64` extraction, but
  the bundled darwin dylib fails to load off the build machine — fixed in 0.0.4.

## 0.0.2 — Windows support fix

Bugfix release. The published `0.0.1` was unusable on Windows and missing a
runtime dependency; both are fixed here. No API changes.

- **Loads on Windows.** The jextract-generated `C_LONG` constant cast the
  canonical C `long` layout to `ValueLayout.OfLong`, which holds on LP64
  (Linux/macOS) but throws `ClassCastException` at class-load on Windows
  (LLP64, where C `long` is 32-bit / `OfInt`). The constant is unused by the
  bindings, so its declared type is widened to the common supertype
  `ValueLayout` — the single committed binding now loads on both ABIs.
- **Native loader reaches consumers.** `0.0.1`'s published POM declared no
  dependencies, so `org.scijava:native-lib-loader` was absent at runtime and
  every call failed with `NoClassDefFoundError`. The publication now carries it
  as a runtime dependency.

## Unreleased — API v1 polish

Pre-1.0 cleanup of the public `mil.army.usace.hec.dss` package. All changes below are **breaking** — clients on a prior snapshot must update call sites before upgrading.

### Breaking — renames

| Before | After |
|---|---|
| `DssTimeSeries.isUndefined(int)` | `DssTimeSeries.isMissing(int)` |
| `DssTimeSeries.dropNa()` | `DssTimeSeries.dropMissing()` |
| `DssTimeSeries.qualityFlags()` | `DssTimeSeries.quality()` |
| `DssPairedData.numberOrdinates()` | `DssPairedData.ordinateCount()` |
| `DssPairedData.numberCurves()` | `DssPairedData.curveCount()` |
| `DssPathname.getPart(Part)` | `DssPathname.part(Part)` |
| `DssGrid.data()` | `DssGrid.values()` |
| `DssGrid.withData(...)` | `DssGrid.withValues(...)` |
| `DssInterval` (class) | `DssIntervals` (class) |
| `HecDss.delete(Path, String)` | `HecDss.deleteRecord(Path, String)` |

Migration: update call sites mechanically — each rename is a 1:1 swap with no semantic change.

### Breaking — factory collapse

`DssTimeSeries` and `DssPairedData` now have private constructors. Use their `static of(...)` factories instead.

```java
// Before
new DssTimeSeries(times, values, "CFS", TimeSeriesDataType.INST_VAL);
new DssPairedData(ordinates, curves, labels, "FEET", "CFS", "Stage", "Flow");

// After
DssTimeSeries.of(times, values, "CFS", TimeSeriesDataType.INST_VAL);
DssPairedData.of(ordinates, curves, labels, "FEET", "CFS", "Stage", "Flow");
```

`DssPairedData.of(...)` now has a multi-curve overload; previously only single-curve was available as a factory.

### Added

- `DssPairedData.curves()` — returns all curves as a 2D array, for symmetry with `ordinates()`.
- `DssGrid.value(int, int)` now validates row/col bounds.
- Richer javadoc on every public accessor in `DssTimeSeries`, `DssPairedData`, `DssGrid`.
- `HecDss` class javadoc explains the `read*` / `get*` naming split and includes a happy-path example.
- Precision note in `DssTimeSeries` javadoc: times are stored at second precision; `Instant` sub-second components are truncated.
- `DssLocationInfo` ctor now coerces `null` description to `""` instead of throwing.

### Fixed

- `DssCatalogEntry` javadoc referenced a nonexistent `getCatalogWithTypes` method; now points at `getCatalog`.
- `HecDss.getCatalog(Path, String)` javadoc used HTML entities for wildcards; now uses `{@literal}`.

### Internal

- `DssGrid.fromNative(...)` and `DssGrid.nativeMetadata()` — two public-but-`@hidden` methods that exposed the internal `NativeGridMetadata` type in the public API — are replaced by the nested `DssGrid.Internal` friend class. Modular clients never see `NativeGridMetadata` now because the `.internal` package is not exported.

### Enforcement

The build now gates merges on:

- **Strict javadoc (`-Xdoclint:all`)** — catches stale `@link`, malformed HTML, broken cross-references.
- **Public API snapshot** — `api/public-api.txt` is regenerated and diffed on every `check`. Any API surface change shows up as a file diff in the PR.
- **Client-perspective module** — `src/clientTest/java/` compiles as a separate JPMS module that only sees exported packages. Catches `.internal` leaks and broken client call sites.

See [`docs/API-DESIGN.md`](docs/API-DESIGN.md) for the rules and [`docs/API-CHECKLIST.md`](docs/API-CHECKLIST.md) for the reviewer gate.
