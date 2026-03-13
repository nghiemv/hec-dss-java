package mil.army.usace.hec.dss;

import mil.army.usace.hec.dss.internal.CatalogReader;
import mil.army.usace.hec.dss.internal.DssSession;
import mil.army.usace.hec.dss.internal.TimeSeriesReader;

import java.util.List;

/**
 * Static entry point for reading DSS files.
 * All methods are self-contained — no resource management required.
 */
public final class HecDss {
    private HecDss() {}

    /**
     * Reads a time series record from a DSS file.
     * The D-part (date window) can be anything — all available data is returned.
     *
     * @param filename the path to the DSS file
     * @param pathname the DSS pathname (e.g. "/FOLSOM/FLOW/01JAN2000/1HOUR/RUN1/")
     * @throws DssException if the file cannot be opened or the record cannot be read
     */
    public static DssTimeSeries readTimeSeries(String filename, String pathname) throws DssException {
        DssPathname parsed = parseAndValidate(pathname);
        DssPathname normalized = parsed.with(DssPathname.Part.D, "*");
        try (DssSession session = DssSession.open(filename)) {
            return TimeSeriesReader.read(session, normalized);
        }
    }

    /**
     * Reads a time series record from a DSS file within a time window.
     * The D-part (date window) can be anything — the time window controls what is returned.
     *
     * @param filename   the path to the DSS file
     * @param pathname   the DSS pathname
     * @param timeWindow the time range to retrieve
     * @throws DssException if the file cannot be opened or the record cannot be read
     */
    public static DssTimeSeries readTimeSeries(String filename, String pathname,
                                               DssTimeWindow timeWindow) throws DssException {
        DssPathname parsed = parseAndValidate(pathname);
        DssPathname normalized = parsed.with(DssPathname.Part.D, "*");
        try (DssSession session = DssSession.open(filename)) {
            return TimeSeriesReader.read(session, normalized, timeWindow);
        }
    }

    /**
     * Returns all pathnames in a DSS file.
     *
     * @param filename the path to the DSS file
     * @throws DssException if the file cannot be opened
     */
    public static List<DssPathname> getCatalog(String filename) throws DssException {
        try (DssSession session = DssSession.open(filename)) {
            return CatalogReader.read(session);
        }
    }

    /**
     * Returns the number of records in a DSS file.
     *
     * @param filename the path to the DSS file
     * @throws DssException if the file cannot be opened
     */
    public static int getRecordCount(String filename) throws DssException {
        try (DssSession session = DssSession.open(filename)) {
            return CatalogReader.recordCount(session);
        }
    }

    private static DssPathname parseAndValidate(String pathname) throws DssException {
        DssPathname parsed = DssPathname.parse(pathname)
                .orElseThrow(() -> new DssException("Invalid DSS pathname: " + pathname));
        if (parsed.hasWildcardRecordParts()) {
            throw new DssException("Pathname contains wildcards in record-identifying parts: " + pathname);
        }
        return parsed;
    }
}
