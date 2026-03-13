package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HecDssTimeSeriesTest {
    @Test
    void retrieveRegularTimeSeries() throws Exception {
        String dssFileName = TestUtil.getResourceFile("examples-all-data-types.dss").toString();
        String pathname = "/regular-time-series/GAPT/FLOW/*/6Hour/forecast1/";
        Instant startTime = ZonedDateTime.parse("2021-09-15T07:00:00Z").toInstant();
        Instant endTime = ZonedDateTime.parse("2021-10-04T07:00:00Z").toInstant();

        DssTimeSeries allValues = HecDss.readTimeSeries(dssFileName, pathname);
        assertEquals(244, allValues.size());
        assertEquals(77, allValues.dropNa().size());

        assertNotNull(allValues.time(0));
        assertNotNull(allValues.units());

        DssTimeWindow timeWindow = new DssTimeWindow(startTime, endTime);
        DssTimeSeries windowed = HecDss.readTimeSeries(dssFileName, pathname, timeWindow);
        assertEquals(77, windowed.size());
    }

    @Test
    void retrieveIrregularTimeSeries() throws Exception {
        String dssFileName = TestUtil.getResourceFile("examples-all-data-types.dss").toString();
        String pathname = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS/";
        Instant startTime = ZonedDateTime.parse("1905-03-20T00:00:00Z").toInstant();
        Instant endTime = ZonedDateTime.parse("2017-02-11T00:00:00Z").toInstant();

        DssTimeSeries allValues = HecDss.readTimeSeries(dssFileName, pathname);
        assertEquals(113, allValues.size());
        assertEquals(113, allValues.dropNa().size());

        DssTimeWindow timeWindow = new DssTimeWindow(startTime, endTime);
        DssTimeSeries windowed = HecDss.readTimeSeries(dssFileName, pathname, timeWindow);
        assertEquals(112, windowed.size());
    }
}
