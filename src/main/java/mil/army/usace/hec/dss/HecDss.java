package mil.army.usace.hec.dss;

import mil.army.usace.hec.dss.internal.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Static entry point for DSS file operations.
 * All methods are self-contained — no resource management required.
 */
public final class HecDss {
    private HecDss() {}

    // ---- Time Series ----

    /**
     * Reads a time series record from a DSS file.
     * Works for both regular and irregular time series.
     */
    public static DssTimeSeries readTimeSeries(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        DssPathname normalized = parsed.with(DssPathname.Part.D, "*");
        try (DssSession session = DssSession.open(file)) {
            expectRecordType(session, parsed, "time series",
                    DssRecordType.REGULAR_TIME_SERIES, DssRecordType.IRREGULAR_TIME_SERIES);
            return TimeSeriesReader.read(session, normalized);
        }
    }

    /**
     * Reads a time series record within a time window.
     * Works for both regular and irregular time series.
     */
    public static DssTimeSeries readTimeSeries(Path file, String pathname,
                                               Instant start, Instant end) {
        DssPathname parsed = parseAndValidate(pathname);
        DssPathname normalized = parsed.with(DssPathname.Part.D, "*");
        try (DssSession session = DssSession.open(file)) {
            expectRecordType(session, parsed, "time series",
                    DssRecordType.REGULAR_TIME_SERIES, DssRecordType.IRREGULAR_TIME_SERIES);
            return TimeSeriesReader.read(session, normalized, start, end);
        }
    }

    /**
     * Writes a time series to a DSS file.
     * Automatically selects regular or irregular storage based on the E-part.
     */
    public static void writeTimeSeries(Path file, String pathname, DssTimeSeries data) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            TimeSeriesWriter.write(session, parsed, data);
        }
    }

    // ---- Paired Data ----

    /**
     * Reads paired data (x/y curves) from a DSS file.
     */
    public static DssPairedData readPairedData(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            expectRecordType(session, parsed, "paired data", DssRecordType.PAIRED_DATA);
            return PairedDataReader.read(session, parsed);
        }
    }

    /**
     * Writes paired data to a DSS file.
     */
    public static void writePairedData(Path file, String pathname, DssPairedData data) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            PairedDataWriter.write(session, parsed, data);
        }
    }

    // ---- Gridded Data ----

    /**
     * Reads a grid record from a DSS file.
     */
    public static DssGrid readGrid(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            expectRecordType(session, parsed, "grid", DssRecordType.GRID);
            return GridReader.read(session, parsed);
        }
    }

    /**
     * Writes a grid record to a DSS file.
     */
    public static void writeGrid(Path file, String pathname, DssGrid data) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            GridWriter.write(session, parsed, data);
        }
    }

    // ---- Array ----

    /**
     * Reads an array record from a DSS file.
     */
    public static double[] readArray(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            return ArrayReader.read(session, parsed);
        }
    }

    /**
     * Writes an array record to a DSS file.
     */
    public static void writeArray(Path file, String pathname, double[] data) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            ArrayWriter.write(session, parsed, data);
        }
    }

    // ---- Text ----

    /**
     * Reads a text record from a DSS file.
     */
    public static String readText(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            return TextReader.read(session, parsed);
        }
    }

    /**
     * Writes a text record to a DSS file.
     */
    public static void writeText(Path file, String pathname, String text) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            TextWriter.write(session, parsed, text);
        }
    }

    // ---- Location Info ----

    /**
     * Reads location metadata from a DSS file.
     */
    public static DssLocationInfo readLocationInfo(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            return LocationInfoReader.read(session, parsed);
        }
    }

    /**
     * Writes location metadata to a DSS file.
     */
    public static void writeLocationInfo(Path file, String pathname, DssLocationInfo info) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            LocationInfoWriter.write(session, parsed, info);
        }
    }

    // ---- Catalog ----

    /**
     * Returns all pathnames in a DSS file.
     */
    public static List<DssPathname> getCatalog(Path file) {
        try (DssSession session = DssSession.open(file)) {
            return CatalogReader.read(session);
        }
    }

    /**
     * Returns all pathnames with their record types in a single pass.
     * More efficient than calling {@link #getRecordType} per pathname.
     */
    public static List<DssCatalogEntry> getCatalogWithTypes(Path file) {
        try (DssSession session = DssSession.open(file)) {
            return CatalogReader.readWithTypes(session);
        }
    }

    /**
     * Returns the number of records in a DSS file.
     */
    public static int getRecordCount(Path file) {
        try (DssSession session = DssSession.open(file)) {
            return CatalogReader.recordCount(session);
        }
    }

    // ---- Record Operations ----

    /**
     * Returns the type of data stored at the given pathname.
     */
    public static DssRecordType getRecordType(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            return RecordTypeReader.read(session, parsed);
        }
    }

    /**
     * Deletes a single record from a DSS file.
     */
    public static void delete(Path file, String pathname) {
        DssPathname parsed = parseAndValidate(pathname);
        try (DssSession session = DssSession.open(file)) {
            DeleteOperation.delete(session, parsed);
        }
    }

    /**
     * Compresses a DSS file, reclaiming space from deleted records.
     */
    public static void squeeze(Path file) {
        SqueezeOperation.squeeze(file);
    }

    private static void expectRecordType(DssSession session, DssPathname pathname,
                                            String expectedLabel, DssRecordType... expected) {
        try {
            DssRecordType actual = RecordTypeReader.read(session, pathname);
            if (actual == DssRecordType.UNKNOWN) return;
            for (DssRecordType e : expected) {
                if (actual == e) return;
            }
            throw new DssException(
                    "Record '%s' is %s, not %s".formatted(pathname, actual, expectedLabel));
        } catch (DssException e) {
            if (e.getMessage().startsWith("Record '")) throw e;
            // Record type check failed (e.g., record not found) — let the reader handle it
        }
    }

    private static DssPathname parseAndValidate(String pathname) {
        DssPathname parsed = DssPathname.parse(pathname)
                .orElseThrow(() -> new DssException(
                        "Invalid DSS pathname format '%s': expected /A/B/C/D/E/F/".formatted(pathname)));
        if ("*".equals(parsed.aPart()) || "*".equals(parsed.bPart()) || "*".equals(parsed.cPart())
                || "*".equals(parsed.ePart()) || "*".equals(parsed.fPart())) {
            throw new DssException(
                    "Pathname '%s' contains wildcards in record-identifying parts (A, B, C, E, or F)"
                            .formatted(pathname));
        }
        return parsed;
    }
}
