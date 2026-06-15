package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HecDssTimeSeriesWriteTest {

    // ---- Regular Time Series ----

    @Test
    void writeAndReadRegularTimeSeries() {
        Path dssFile = TestUtil.createTempFile("write-test.dss");
        String pathname = "/TEST/LOCATION/FLOW/01Jan2020/1Hour/WRITE-TEST/";

        Instant start = ZonedDateTime.parse("2020-01-01T00:00:00Z").toInstant();
        double[] values = {100.0, 200.0, 300.0, 400.0, 500.0};
        Instant[] times = new Instant[5];
        for (int i = 0; i < 5; i++) {
            times[i] = Instant.ofEpochSecond(start.getEpochSecond() + i * 3600);
        }

        DssTimeSeries input = DssTimeSeries.of(times, values, "CFS", TimeSeriesDataType.INST_VAL);
        HecDss.writeTimeSeries(dssFile, pathname, input);

        DssTimeSeries output = HecDss.readTimeSeries(dssFile, pathname);
        assertEquals(5, output.size());
        assertEquals(100.0, output.value(0));
        assertEquals(500.0, output.value(4));
        assertEquals("CFS", output.units());
        assertEquals(TimeSeriesDataType.INST_VAL, output.type());
    }

    @Test
    void readRegularTimeSeriesThenWriteToNewPathThenRead() {
        Path dssFile = TestUtil.copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/regular-time-series/GAPT/FLOW/*/6Hour/forecast1/";

        Instant t1 = ZonedDateTime.parse("2021-10-01T07:00:00Z").toInstant();
        Instant t2 = ZonedDateTime.parse("2021-10-04T07:00:00Z").toInstant();

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath, t1, t2);
        int originalSize = original.size();
        assertTrue(originalSize > 0);

        String writePath = "/regular-time-series/GAPT/FLOW/01Oct2021/6Hour/test-store/";
        HecDss.writeTimeSeries(dssFile, writePath, original);

        DssTimeSeries reread = HecDss.readTimeSeries(dssFile, writePath);
        assertEquals(originalSize, reread.size());
        assertEquals(original.units(), reread.units());
        assertEquals(original.type(), reread.type());
        assertArrayEquals(original.values(), reread.values());
    }

    @Test
    void readModifyWriteReadRegularTimeSeries() {
        Path dssFile = TestUtil.copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/regular-time-series/GAPT/FLOW/*/6Hour/forecast1/";
        Instant t1 = ZonedDateTime.parse("2021-10-01T07:00:00Z").toInstant();
        Instant t2 = ZonedDateTime.parse("2021-10-04T07:00:00Z").toInstant();

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath, t1, t2);

        double[] modifiedValues = original.values().clone();
        modifiedValues[3] = 75.0;
        DssTimeSeries modified = DssTimeSeries.of(original.times(), modifiedValues, "FEET", original.type());

        String writePath = "/regular-time-series/GAPT/FLOW/01Oct2021/6Hour/test-modified/";
        HecDss.writeTimeSeries(dssFile, writePath, modified);

        DssTimeSeries reread = HecDss.readTimeSeries(dssFile, writePath);
        assertEquals(original.size(), reread.size());
        assertEquals(75.0, reread.value(3));
        assertEquals("FEET", reread.units());
    }

    // ---- Irregular Time Series ----

    @Test
    void writeAndReadIrregularTimeSeries() {
        Path dssFile = TestUtil.createTempFile("write-irreg-test.dss");
        String pathname = "/TEST/LOCATION/FLOW-PEAK/01Jan1990/IR-Century/WRITE-TEST/";

        double[] values = {1500.0, 2300.0, 1800.0};
        Instant[] times = {
                ZonedDateTime.parse("1995-03-15T12:00:00Z").toInstant(),
                ZonedDateTime.parse("2001-06-20T08:00:00Z").toInstant(),
                ZonedDateTime.parse("2010-11-05T16:00:00Z").toInstant()
        };

        DssTimeSeries input = DssTimeSeries.of(times, values, "CFS", TimeSeriesDataType.INST_VAL);
        HecDss.writeTimeSeries(dssFile, pathname, input);

        DssTimeSeries output = HecDss.readTimeSeries(dssFile, pathname);
        assertEquals(3, output.size());
        assertEquals(1500.0, output.value(0), 1.0);
        assertEquals(2300.0, output.value(1), 1.0);
        assertEquals(1800.0, output.value(2), 1.0);
    }

    @Test
    void readIrregularTimeSeriesThenWriteToNewPathThenRead() {
        Path dssFile = TestUtil.copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS/";

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath);
        assertEquals(113, original.size());

        String writePath = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS-copy/";
        HecDss.writeTimeSeries(dssFile, writePath, original);

        DssTimeSeries reread = HecDss.readTimeSeries(dssFile, writePath);
        assertEquals(original.size(), reread.size());
        assertArrayEquals(original.values(), reread.values());
        assertEquals(original.units(), reread.units());
        assertEquals(original.type(), reread.type());
    }

    @Test
    void readModifyWriteReadIrregularTimeSeries() {
        Path dssFile = TestUtil.copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS/";

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath);

        double[] modifiedValues = original.values().clone();
        modifiedValues[3] = 75.0;
        DssTimeSeries modified = DssTimeSeries.of(original.times(), modifiedValues, "FEET", original.type());

        String writePath = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS-modified/";
        HecDss.writeTimeSeries(dssFile, writePath, modified);

        DssTimeSeries reread = HecDss.readTimeSeries(dssFile, writePath);
        assertEquals(original.size(), reread.size());
        assertEquals(75.0, reread.value(3));
        assertEquals("FEET", reread.units());
    }
}
