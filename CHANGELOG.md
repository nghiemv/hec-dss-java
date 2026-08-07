# Changelog

## 0.0.5

- **Control over native diagnostics.** `HecDss.setMessageLevel(DssMessageLevel)`
  sets how much the native library prints; `HecDss.setLogFile(Path)` and
  `HecDss.logToConsole()` choose where it goes. Both are process-global, matching
  the underlying heclib settings. The native default (`GENERAL`) prints an open
  header and a nineteen-line close-statistics block around every file operation,
  which dominates the console output of any application that reads DSS in a loop.
  Since this API reports failures as `DssException`, callers can drop to
  `CRITICAL`. A handful of native messages are written unconditionally and ignore
  the level — notably `"Error reading record type from path:"`, which heclib emits
  on the ordinary lookup miss behind `recordExists` — so `setLogFile` is the way
  to get a fully quiet console.

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
