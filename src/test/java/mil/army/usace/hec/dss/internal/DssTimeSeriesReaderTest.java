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
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            assertEquals(244, allValues.size());
            assertEquals(77, allValues.dropNa().size());

            // Verify indexed access works
            assertNotNull(allValues.time(0));
            assertNotNull(allValues.dataUnits());

            DssTimeWindow timeWindow = new DssTimeWindow(startTime, endTime);
            DssTimeSeries windowed = hecDss.getTimeSeries(pathname, timeWindow);
            assertEquals(77, windowed.size());
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
            assertEquals(113, allValues.size());
            assertEquals(113, allValues.dropNa().size());

            DssTimeWindow timeWindow = new DssTimeWindow(startTime, endTime);
            DssTimeSeries windowed = hecDss.getTimeSeries(pathname, timeWindow);
            assertEquals(112, windowed.size());
        }
    }
}
