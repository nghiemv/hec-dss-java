package mil.army.usace.hec.dss;

import mil.army.usace.hec.dss.internal.HecDssImpl;

import java.util.stream.Stream;

/**
 * Main interface for working with DSS files.
 */
public interface HecDss extends AutoCloseable {

    /**
     * Gets a stream of all pathnames in the DSS file.
     */
    Stream<DssPathname> getCatalog();

    /**
     * Gets the number of records stored in the DSS file.
     */
    int getRecordCount();

    /**
     * Retrieves time series data from the DSS file.
     */
    DssTimeSeries getTimeSeries(DssPathname pathname);

    /**
     * Retrieves time series data from the DSS file within a time window.
     */
    DssTimeSeries getTimeSeries(DssPathname pathname, DssTimeWindow timeWindow);

    /**
     * Closes the DSS file and releases any locks.
     */
    @Override
    void close();

    /**
     * Creates a new HecDss instance for the specified file.
     */
    static HecDss open(String filename) {
        return HecDssImpl.open(filename);
    }
}
