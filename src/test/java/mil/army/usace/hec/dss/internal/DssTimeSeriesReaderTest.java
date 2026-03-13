package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssTimeSeries;
import mil.army.usace.hec.dss.DssTimeWindow;
import mil.army.usace.hec.dss.HecDss;
import mil.army.usace.hec.dss.TestUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DssTimeSeriesReaderTest {
    @Test
    void retrieveRegularTimeSeries() {
        String dssFileName = TestUtil.getResourceFile("examples-all-data-types.dss").toString();
        String dssPathname = "/regular-time-series/GAPT/FLOW/*/6Hour/forecast1/";
        Instant startTime = ZonedDateTime.parse("2021-09-15T07:00:00Z").toInstant();
        Instant endTime = ZonedDateTime.parse("2021-10-04T07:00:00Z").toInstant();

        try (HecDss hecDss = HecDss.open(dssFileName)) {
            DssPathname pathname = DssPathname.parse(dssPathname).orElseThrow();

            DssTimeSeries allValues = hecDss.getTimeSeries(pathname);
            assertEquals(244, allValues.times().length);
            assertEquals(77, allValues.dropNa().times().length);

            DssTimeWindow timeWindow = new DssTimeWindow(startTime, endTime);
            DssTimeSeries windowed = hecDss.getTimeSeries(pathname, timeWindow);
            assertEquals(77, windowed.times().length);
        }
    }

    @Test
    void retrieveIrregularTimeSeries() {
        String dssFileName = TestUtil.getResourceFile("examples-all-data-types.dss").toString();
        String dssPathname = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS/";
        Instant startTime = ZonedDateTime.parse("1905-03-20T00:00:00Z").toInstant();
        Instant endTime = ZonedDateTime.parse("2017-02-11T00:00:00Z").toInstant();

        try (HecDss hecDss = HecDss.open(dssFileName)) {
            DssPathname pathname = DssPathname.parse(dssPathname).orElseThrow();

            DssTimeSeries allValues = hecDss.getTimeSeries(pathname);
            assertEquals(113, allValues.times().length);
            assertEquals(113, allValues.dropNa().times().length);

            DssTimeWindow timeWindow = new DssTimeWindow(startTime, endTime);
            DssTimeSeries windowed = hecDss.getTimeSeries(pathname, timeWindow);
            assertEquals(112, windowed.times().length);
        }
    }
}
