# Changelog

## 0.0.4

Initial release of the typed `hec-dss-java` API.

- **Typed API over libhecdss via the Java FFM API.** `HecDss` is the single
  entry point, with `read*` / `write*` / `get*` methods returning plain value
  objects (time series, paired data, grids, arrays, text, location info,
  catalog). FFM bindings are generated with jextract from the `hecdss.h` header
  bundled in the native zip, so they cannot drift from the binary.
- **Bundled natives.** Windows (x86_64), Linux (x86_64), and macOS (x86_64,
  Rosetta on Apple Silicon) `hecdss` natives ship in the jar and load
  automatically. The macOS dylib's zlib `@rpath` is repaired at build time to
  resolve the system `libz`, so the published artifact loads on any Mac.
- **JPMS module** `mil.army.usace.hec.dss`, with a client-perspective compile
  check and a committed public-API snapshot that fails the build on drift.
- **CI** builds and tests on Linux, macOS, and Windows; releases publish to HEC
  Nexus.
