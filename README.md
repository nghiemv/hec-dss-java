# hec-dss-java

A small, typed Java API for reading and writing [HEC-DSS](https://www.hec.usace.army.mil/software/hec-dss/)
files, built on the `hecdss` C library through the Java Foreign Function &
Memory (FFM) API. The native binaries for Windows, Linux, and macOS are bundled
in the jar and loaded automatically — no separate install, no `java.library.path`
setup.

## Usage

`HecDss` is the only entry point. Every operation is a static call that takes the
DSS file `Path` and a pathname; reads return plain value objects, writes take them.

```java
import mil.army.usace.hec.dss.*;
import java.nio.file.Path;

Path dss = Path.of("example.dss");

// Write a regular-interval time series
DssTimeSeries ts = DssTimeSeries.of(times, values, "CFS", TimeSeriesDataType.INST_VAL);
HecDss.writeTimeSeries(dss, "/BASIN/GAGE/FLOW//1Hour/OBS/", ts);

// Read it back
DssTimeSeries back = HecDss.readTimeSeries(dss, "/BASIN/GAGE/FLOW//1Hour/OBS/");

// Catalog, record type, existence, delete
List<DssCatalogEntry> records = HecDss.getCatalog(dss);
DssRecordType type = HecDss.getRecordType(dss, "/BASIN/GAGE/FLOW//1Hour/OBS/");
boolean present = HecDss.recordExists(dss, "/BASIN/GAGE/FLOW//1Hour/OBS/");
HecDss.deleteRecord(dss, "/BASIN/GAGE/FLOW//1Hour/OBS/");
```

Time series, paired data, grids, arrays, text, and location info each have
`read*` / `write*` methods; catalog and metadata use `get*`. See
[docs/API-DESIGN.md](docs/API-DESIGN.md) for the conventions (value objects,
private constructors with `of(...)` factories, `NaN` for missing values, singular
vs. plural accessors).

## Requirements

- **Java 25+** (the FFM API and the bundled bindings target Java 25).
- The library is a JPMS module: `mil.army.usace.hec.dss`.

## Build

```bash
./gradlew build      # compile, test, javadoc, and the public-API snapshot check
```

The build resolves the `hecdss` native zips from HEC Nexus and stages them into
the jar under `natives/<platform>/`. Gradle provisions the JDK and Node toolchains
itself; no manual setup is required.

## Platform support

Windows (x86_64), Linux (x86_64), and macOS (x86_64; Apple Silicon runs under
Rosetta) natives are bundled. The macOS dylib's `@rpath` for zlib is repaired at
build time so it resolves the system `libz` on any Mac — see the
`patchMacosNativeRpath` task in [build.gradle.kts](build.gradle.kts).

## License

[MIT](LICENSE)
