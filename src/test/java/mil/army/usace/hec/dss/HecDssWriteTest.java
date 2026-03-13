package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HecDssWriteTest {
    @Test
    void writeAndReadRegularTimeSeries() {
        String dssFile = TestUtil.createTempFile("write-test.dss");
        String pathname = "/TEST/LOCATION/FLOW/01Jan2020/1Hour/WRITE-TEST/";

        Instant start = ZonedDateTime.parse("2020-01-01T00:00:00Z").toInstant();
        double[] values = {100.0, 200.0, 300.0, 400.0, 500.0};
        long[] times = new long[5];
        for (int i = 0; i < 5; i++) {
            times[i] = start.getEpochSecond() + i * 3600;
        }

        DssTimeSeries input = new DssTimeSeries(values, times, "CFS", "INST-VAL");
        HecDss.writeTimeSeries(dssFile, pathname, input);

        DssTimeSeries output = HecDss.readTimeSeries(dssFile, pathname);
        assertEquals(5, output.size());
        assertEquals(100.0, output.value(0));
        assertEquals(500.0, output.value(4));
        assertEquals("CFS", output.units());
        assertEquals("INST-VAL", output.type());
    }

    @Test
    void writeAndReadIrregularTimeSeries() {
        String dssFile = TestUtil.createTempFile("write-irreg-test.dss");
        String pathname = "/TEST/LOCATION/FLOW-PEAK/01Jan1990/IR-Century/WRITE-TEST/";

        double[] values = {1500.0, 2300.0, 1800.0};
        long[] times = {
                ZonedDateTime.parse("1995-03-15T12:00:00Z").toInstant().getEpochSecond(),
                ZonedDateTime.parse("2001-06-20T08:00:00Z").toInstant().getEpochSecond(),
                ZonedDateTime.parse("2010-11-05T16:00:00Z").toInstant().getEpochSecond()
        };

        DssTimeSeries input = new DssTimeSeries(values, times, "CFS", "INST-VAL");
        HecDss.writeTimeSeries(dssFile, pathname, input);

        DssTimeSeries output = HecDss.readTimeSeries(dssFile, pathname);
        assertEquals(3, output.size());
        assertEquals(1500.0, output.value(0), 1.0);
        assertEquals(2300.0, output.value(1), 1.0);
        assertEquals(1800.0, output.value(2), 1.0);
    }

    @Test
    void writeAndReadText() {
        String dssFile = TestUtil.createTempFile("text-test.dss");
        String pathname = "/TEST/LOCATION/NOTE//TEXT/VERSION/";

        String text = "Hello, DSS! This is a test text record.";
        HecDss.writeText(dssFile, pathname, text);

        String output = HecDss.readText(dssFile, pathname);
        assertEquals(text, output);
    }

    @Test
    void writeAndReadPairedData() {
        String dssFile = TestUtil.createTempFile("paired-test.dss");
        String pathname = "/TEST/LOCATION/STAGE-FLOW///RATING/";

        double[] ordinates = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] values = {0.0, 10.0, 50.0, 150.0, 300.0};
        DssPairedData input = new DssPairedData(
                ordinates, values, 1, new String[]{"Rating Curve"},
                "FEET", "CFS", "Stage", "Flow"
        );

        HecDss.writePairedData(dssFile, pathname, input);

        DssPairedData output = HecDss.readPairedData(dssFile, pathname);
        assertEquals(5, output.numberOrdinates());
        assertEquals(1, output.numberCurves());
        assertEquals(0.0, output.ordinates()[0]);
        assertEquals(300.0, output.values()[4]);
        assertEquals("FEET", output.xUnits());
        assertEquals("CFS", output.yUnits());
    }

    @Test
    void writeAndReadArray() {
        String dssFile = TestUtil.createTempFile("array-test.dss");
        String pathname = "/TEST/LOCATION/DATA///ARRAY-TEST/";

        DssArray input = new DssArray(
                new int[]{1, 2, 3, 4, 5},
                new float[]{1.1f, 2.2f, 3.3f},
                new double[]{100.0, 200.0}
        );

        HecDss.writeArray(dssFile, pathname, input);

        DssArray output = HecDss.readArray(dssFile, pathname);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, output.intValues());
        assertArrayEquals(new float[]{1.1f, 2.2f, 3.3f}, output.floatValues());
        assertArrayEquals(new double[]{100.0, 200.0}, output.doubleValues());
    }

    @Test
    void writeAndReadLocationInfo() {
        String dssFile = TestUtil.createTempFile("location-test.dss");
        String pathname = "/TEST/LOCATION/DATA///LOC-TEST/";

        DssLocationInfo input = new DssLocationInfo(
                -121.5, 38.5, 100.0,
                0, 0, 0, 0, 0, 0,
                "UTC", "Test supplemental info"
        );

        HecDss.writeLocationInfo(dssFile, pathname, input);

        DssLocationInfo output = HecDss.readLocationInfo(dssFile, pathname);
        assertEquals(-121.5, output.x(), 0.001);
        assertEquals(38.5, output.y(), 0.001);
        assertEquals(100.0, output.z(), 0.001);
        assertEquals("Test supplemental info", output.supplemental());
    }

    @Test
    void deleteRecord() {
        String dssFile = TestUtil.createTempFile("delete-test.dss");
        String pathname = "/TEST/LOCATION/NOTE//TEXT/TO-DELETE/";

        HecDss.writeText(dssFile, pathname, "This will be deleted");
        assertTrue(HecDss.getRecordCount(dssFile) > 0);

        HecDss.delete(dssFile, pathname);
    }
}
