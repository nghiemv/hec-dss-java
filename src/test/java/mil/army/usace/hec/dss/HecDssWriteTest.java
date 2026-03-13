package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HecDssWriteTest {

    // ---- Regular Time Series ----

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
    void readRegularTimeSeriesThenWriteToNewPathThenRead() throws Exception {
        String dssFile = copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/regular-time-series/GAPT/FLOW/*/6Hour/forecast1/";

        Instant t1 = ZonedDateTime.parse("2021-10-01T07:00:00Z").toInstant();
        Instant t2 = ZonedDateTime.parse("2021-10-04T07:00:00Z").toInstant();

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath, new DssTimeWindow(t1, t2));
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
    void readModifyWriteReadRegularTimeSeries() throws Exception {
        String dssFile = copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/regular-time-series/GAPT/FLOW/*/6Hour/forecast1/";
        Instant t1 = ZonedDateTime.parse("2021-10-01T07:00:00Z").toInstant();
        Instant t2 = ZonedDateTime.parse("2021-10-04T07:00:00Z").toInstant();

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath, new DssTimeWindow(t1, t2));

        // Modify values
        double[] modifiedValues = original.values().clone();
        modifiedValues[3] = 75.0;
        DssTimeSeries modified = new DssTimeSeries(modifiedValues, original.epochSeconds(), "FEET", original.type());

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
    void readIrregularTimeSeriesThenWriteToNewPathThenRead() throws Exception {
        String dssFile = copyResourceToTemp("examples-all-data-types.dss");
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
    void readModifyWriteReadIrregularTimeSeries() throws Exception {
        String dssFile = copyResourceToTemp("examples-all-data-types.dss");
        String readPath = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS/";

        DssTimeSeries original = HecDss.readTimeSeries(dssFile, readPath);

        double[] modifiedValues = original.values().clone();
        modifiedValues[3] = 75.0;
        DssTimeSeries modified = new DssTimeSeries(modifiedValues, original.epochSeconds(), "FEET", original.type());

        String writePath = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS-modified/";
        HecDss.writeTimeSeries(dssFile, writePath, modified);

        DssTimeSeries reread = HecDss.readTimeSeries(dssFile, writePath);
        assertEquals(original.size(), reread.size());
        assertEquals(75.0, reread.value(3));
        assertEquals("FEET", reread.units());
    }

    // ---- Paired Data ----

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
        assertArrayEquals(ordinates, output.ordinates());
        assertArrayEquals(values, output.values());
        assertEquals("FEET", output.xUnits());
        assertEquals("CFS", output.yUnits());
    }

    @Test
    void readPairedDataFromExistingFile() throws Exception {
        String dssFile = copyResourceToTemp("sample7.dss");
        String pathname = "/MY BASIN/DEER CREEK/STAGE-FLOW///USGS/";

        DssPairedData pd = HecDss.readPairedData(dssFile, pathname);
        assertTrue(pd.numberOrdinates() > 0);
        assertEquals(1, pd.numberCurves());
        assertEquals("FEET", pd.xUnits());
        assertEquals("CFS", pd.yUnits());
    }

    @Test
    void readPairedDataWriteToNewPathRead() throws Exception {
        String dssFile = copyResourceToTemp("sample7.dss");
        String readPath = "/MY BASIN/DEER CREEK/STAGE-FLOW///USGS/";
        String writePath = "/MY BASIN/DEER CREEK/STAGE-FLOW///USGS-copy/";

        DssPairedData original = HecDss.readPairedData(dssFile, readPath);

        HecDss.writePairedData(dssFile, writePath, original);

        DssPairedData reread = HecDss.readPairedData(dssFile, writePath);
        assertArrayEquals(original.ordinates(), reread.ordinates());
        assertArrayEquals(original.values(), reread.values());
        assertEquals(original.xUnits(), reread.xUnits());
        assertEquals(original.yUnits(), reread.yUnits());
    }

    @Test
    void writeMultiCurvePairedDataThenRead() {
        String dssFile = TestUtil.createTempFile("multi-curve-test.dss");
        String pathname = "/TEST/LOCATION/STAGE-FLOW///MULTI-CURVE/";

        double[] ordinates = {0.0, 11.0, 22.0, 33.0, 44.0};
        // 3 curves: column-major layout = ordinates * curves
        double[] values = new double[ordinates.length * 3];
        for (int i = 0; i < ordinates.length; i++) {
            for (int c = 0; c < 3; c++) {
                values[i * 3 + c] = ordinates[i] + c;
            }
        }
        String[] labels = {"x plus 0", "x plus 1", "x plus 2"};
        DssPairedData input = new DssPairedData(
                ordinates, values, 3, labels,
                "cm", "CFS", "Stage", "Flow"
        );

        HecDss.writePairedData(dssFile, pathname, input);

        DssPairedData output = HecDss.readPairedData(dssFile, pathname);
        assertEquals(5, output.numberOrdinates());
        assertEquals(3, output.numberCurves());
        assertArrayEquals(ordinates, output.ordinates());
        assertArrayEquals(values, output.values());
    }

    @Test
    void readMultiCurvePairedDataFromExistingFile() throws Exception {
        String dssFile = copyResourceToTemp("R703F3-PF_v7.dss");
        String pathname = "/FOLSOM/AUXILIARY SPILLWAY-GATE RATING/ELEV-FLOW/PAIREDVALUESEXT///";

        DssPairedData pd = HecDss.readPairedData(dssFile, pathname);
        assertTrue(pd.numberOrdinates() > 0);
        assertTrue(pd.numberCurves() > 1, "Expected multi-curve paired data");
        assertTrue(pd.labels().length > 0, "Expected labels on multi-curve data");
    }

    @Test
    void readMultiCurvePairedDataModifyLabelsRoundTrip() throws Exception {
        String dssFile = copyResourceToTemp("R703F3-PF_v7.dss");
        String readPath = "/FOLSOM/AUXILIARY SPILLWAY-GATE RATING/ELEV-FLOW/PAIREDVALUESEXT///";
        String writePath = "/FOLSOM/AUXILIARY SPILLWAY-GATE RATING/ELEV-FLOW/PAIREDVALUESEXT//label-test/";

        DssPairedData original = HecDss.readPairedData(dssFile, readPath);

        // Modify a label
        String[] newLabels = original.labels().clone();
        newLabels[Math.min(3, newLabels.length - 1)] = "New Label";
        DssPairedData modified = new DssPairedData(
                original.ordinates(), original.values(), original.numberCurves(),
                newLabels, original.xUnits(), original.yUnits(),
                original.xType(), original.yType()
        );

        HecDss.writePairedData(dssFile, writePath, modified);

        DssPairedData reread = HecDss.readPairedData(dssFile, writePath);
        assertArrayEquals(original.ordinates(), reread.ordinates());
        assertArrayEquals(original.values(), reread.values());
        assertEquals("New Label", reread.labels()[Math.min(3, reread.labels().length - 1)]);
    }

    // ---- Grid ----

    @Test
    void readGridFromExistingFile() throws Exception {
        String dssFile = copyResourceToTemp("grid-example.dss");
        String pathname = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS/";

        DssGrid grid = HecDss.readGrid(dssFile, pathname);
        assertEquals(21, grid.numberOfCellsX());
        assertEquals(28, grid.numberOfCellsY());
        assertEquals(21 * 28, grid.data().length);
    }

    @Test
    void readGridWriteToNewPathThenRead() throws Exception {
        String dssFile = copyResourceToTemp("grid-example.dss");
        String readPath = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS/";
        String writePath = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS-copy/";

        DssGrid original = HecDss.readGrid(dssFile, readPath);
        HecDss.writeGrid(dssFile, writePath, original);

        DssGrid reread = HecDss.readGrid(dssFile, writePath);
        assertArrayEquals(original.data(), reread.data());
        assertEquals(original.numberOfCellsX(), reread.numberOfCellsX());
        assertEquals(original.numberOfCellsY(), reread.numberOfCellsY());
    }

    @Test
    void writeNewGridThenRead() {
        String dssFile = TestUtil.createTempFile("grid-write-test.dss");
        String pathname = "/grid/new/gradient/01MAY2024:1400/01MAY2024:1400/new-grad/";

        int cellsX = 50, cellsY = 50;
        float[] data = new float[cellsX * cellsY];
        for (int i = 0; i < cellsY; i++) {
            for (int j = 0; j < cellsX; j++) {
                data[i * cellsX + j] = j + (50 * i);
            }
        }

        DssGrid input = new DssGrid(data,
                420, 1, 0, 0, cellsX, cellsY, 0,
                2000.0f, 0.0f, 0.0f, false, false, "", 0,
                "WKT", "", 0,
                0.0f, 2499.0f, 0.0f, 1249.5f,
                new float[0], new int[0]);

        HecDss.writeGrid(dssFile, pathname, input);

        DssGrid output = HecDss.readGrid(dssFile, pathname);
        assertEquals(cellsX, output.numberOfCellsX());
        assertEquals(cellsY, output.numberOfCellsY());
        assertArrayEquals(data, output.data());
    }

    // ---- Array ----

    @Test
    void writeAndReadArrayFloatOnly() {
        String dssFile = TestUtil.createTempFile("array-float-test.dss");
        String pathname = "/test/float-array/redshift////";

        DssArray input = new DssArray(new int[0], new float[]{1.0f, 3.0f, 5.0f, 7.0f}, new double[0]);
        HecDss.writeArray(dssFile, pathname, input);

        DssArray output = HecDss.readArray(dssFile, pathname);
        assertArrayEquals(new float[]{1.0f, 3.0f, 5.0f, 7.0f}, output.floatValues());
    }

    @Test
    void writeAndReadArrayAllThreeTypes() {
        String dssFile = TestUtil.createTempFile("array-all-test.dss");
        String pathname = "/TEST/LOCATION/DATA///ARRAY-TEST/";

        int[] ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        float[] floats = {1.0f, 3.0f, 5.0f, 7.0f};
        double[] doubles = {0.0, 32.3, 64.6, 96.9, 129.2, 161.5, 193.8, 226.1};
        DssArray input = new DssArray(ints, floats, doubles);
        HecDss.writeArray(dssFile, pathname, input);

        DssArray output = HecDss.readArray(dssFile, pathname);
        assertArrayEquals(ints, output.intValues());
        assertArrayEquals(floats, output.floatValues());
        assertArrayEquals(doubles, output.doubleValues(), 0.01);
    }

    @Test
    void writeModifyWriteReadArray() {
        String dssFile = TestUtil.createTempFile("array-modify-test.dss");
        String path1 = "/TEST/LOCATION/DATA///ARRAY-ORIG/";
        String path2 = "/TEST/LOCATION/DATA///ARRAY-MODIFIED/";

        DssArray input = new DssArray(
                new int[]{1, 2, 3},
                new float[]{1.0f, 2.0f},
                new double[]{10.0, 20.0}
        );
        HecDss.writeArray(dssFile, path1, input);

        DssArray read = HecDss.readArray(dssFile, path1);

        // Modify doubles
        double[] modifiedDoubles = new double[read.doubleValues().length];
        for (int i = 0; i < modifiedDoubles.length; i++) {
            modifiedDoubles[i] = read.doubleValues()[i] * 2;
        }
        DssArray modified = new DssArray(read.intValues(), read.floatValues(), modifiedDoubles);
        HecDss.writeArray(dssFile, path2, modified);

        DssArray reread = HecDss.readArray(dssFile, path2);
        assertArrayEquals(new int[]{1, 2, 3}, reread.intValues());
        assertArrayEquals(new float[]{1.0f, 2.0f}, reread.floatValues());
        assertArrayEquals(new double[]{20.0, 40.0}, reread.doubleValues(), 0.01);
    }

    // ---- Text ----

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
    void writeAndReadMultilineText() {
        String dssFile = TestUtil.createTempFile("text-multiline-test.dss");
        String pathname = "/A/B/C/D/E/F/";

        String text = "This is a test\nof text data\nin a DSS file.\n";
        HecDss.writeText(dssFile, pathname, text);

        String output = HecDss.readText(dssFile, pathname);
        assertEquals(text, output);
    }

    // ---- Location Info ----

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
    void readLocationInfoFromExistingFile() throws Exception {
        String dssFile = copyResourceToTemp("sample7.dss");
        String pathname = "/MISSISSIPPI/ST. LOUIS/Location Info////";

        DssLocationInfo loc = HecDss.readLocationInfo(dssFile, pathname);
        // Just verify it reads without error and has coordinates
        assertNotNull(loc.timeZoneName());
    }

    @Test
    void readLocationInfoWriteToNewPathRead() throws Exception {
        String dssFile = copyResourceToTemp("sample7.dss");
        String readPath = "/MISSISSIPPI/ST. LOUIS/Location Info////";
        String writePath = "/MISSISSIPPI/ST. LOUIS/Location Info///newPath/";

        DssLocationInfo original = HecDss.readLocationInfo(dssFile, readPath);
        HecDss.writeLocationInfo(dssFile, writePath, original);

        DssLocationInfo reread = HecDss.readLocationInfo(dssFile, writePath);
        assertEquals(original.x(), reread.x(), 0.001);
        assertEquals(original.y(), reread.y(), 0.001);
        assertEquals(original.z(), reread.z(), 0.001);
        assertEquals(original.coordinateSystem(), reread.coordinateSystem());
        assertEquals(original.coordinateId(), reread.coordinateId());
        assertEquals(original.horizontalUnits(), reread.horizontalUnits());
        assertEquals(original.horizontalDatum(), reread.horizontalDatum());
        assertEquals(original.verticalUnits(), reread.verticalUnits());
        assertEquals(original.verticalDatum(), reread.verticalDatum());
        assertEquals(original.timeZoneName(), reread.timeZoneName());
        assertEquals(original.supplemental(), reread.supplemental());
    }

    @Test
    void readModifyWriteReadLocationInfo() throws Exception {
        String dssFile = copyResourceToTemp("sample7.dss");
        String readPath = "/MISSISSIPPI/ST. LOUIS/Location Info////";
        String writePath = "/MISSISSIPPI/ST. LOUIS/Location Info///modified/";

        DssLocationInfo original = HecDss.readLocationInfo(dssFile, readPath);

        DssLocationInfo modified = new DssLocationInfo(
                original.x() + 1.0, original.y(), original.z(),
                original.coordinateSystem(), original.coordinateId(),
                original.horizontalUnits(), original.horizontalDatum(),
                original.verticalUnits(), original.verticalDatum(),
                original.timeZoneName(), original.supplemental()
        );
        HecDss.writeLocationInfo(dssFile, writePath, modified);

        DssLocationInfo reread = HecDss.readLocationInfo(dssFile, writePath);
        assertEquals(original.x() + 1.0, reread.x(), 0.001);
    }

    // ---- Delete ----

    @Test
    void deleteRecordVerifyGone() throws Exception {
        String dssFile = copyResourceToTemp("sample7.dss");
        String pathname = "//SACRAMENTO/PRECIP-INC//1Day/OBS/";

        // Read to verify it exists
        Instant t1 = ZonedDateTime.parse("2005-01-01T00:00:00Z").toInstant();
        Instant t2 = ZonedDateTime.parse("2005-01-04T00:00:00Z").toInstant();
        DssTimeSeries ts = HecDss.readTimeSeries(dssFile, pathname, new DssTimeWindow(t1, t2));
        assertTrue(ts.size() > 0);

        // Write to new path, then delete it
        String newPath = "//SACRAMENTO/PRECIP-INC//1Day/OBS-to-delete/";
        HecDss.writeTimeSeries(dssFile, newPath, ts);

        // Verify written
        DssTimeSeries written = HecDss.readTimeSeries(dssFile, newPath, new DssTimeWindow(t1, t2));
        assertEquals(ts.size(), written.size());

        // Delete all dated variants
        var catalog = HecDss.getCatalog(dssFile);
        for (DssPathname p : catalog) {
            if (p.fPart().equals("OBS-to-delete")) {
                HecDss.delete(dssFile, p.toString());
            }
        }

        // Verify deleted — reading should throw or return empty
        assertThrows(DssException.class, () ->
                HecDss.readTimeSeries(dssFile, newPath, new DssTimeWindow(t1, t2)));
    }

    @Test
    void deleteTextRecord() {
        String dssFile = TestUtil.createTempFile("delete-text-test.dss");
        String pathname = "/TEST/LOCATION/NOTE//TEXT/TO-DELETE/";

        HecDss.writeText(dssFile, pathname, "This will be deleted");
        assertTrue(HecDss.getRecordCount(dssFile) > 0);

        HecDss.delete(dssFile, pathname);
    }

    // ---- Helpers ----

    private static String copyResourceToTemp(String resourceName) throws Exception {
        Path source = TestUtil.getResourceFile(resourceName);
        Path temp = Files.createTempFile(resourceName.replace(".dss", ""), ".dss");
        Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
        return temp.toAbsolutePath().toString();
    }
}
