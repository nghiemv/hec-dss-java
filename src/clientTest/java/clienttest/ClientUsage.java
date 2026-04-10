package clienttest;

import mil.army.usace.hec.dss.DssCatalogEntry;
import mil.army.usace.hec.dss.DssCrs;
import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssGrid;
import mil.army.usace.hec.dss.DssIntervals;
import mil.army.usace.hec.dss.DssLocationInfo;
import mil.army.usace.hec.dss.DssPairedData;
import mil.army.usace.hec.dss.DssParameters;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssRecordType;
import mil.army.usace.hec.dss.DssTimeSeries;
import mil.army.usace.hec.dss.DssUnits;
import mil.army.usace.hec.dss.GridDataType;
import mil.army.usace.hec.dss.HecDss;
import mil.army.usace.hec.dss.TimeSeriesDataType;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Exercises every public API entry point. This code never runs — it exists
 * purely so the compiler tells us when a rename, deletion, or internal leak
 * would break a client.
 */
@SuppressWarnings("unused")
public final class ClientUsage {
    private ClientUsage() {}

    static void readRecords(Path file) {
        DssTimeSeries ts = HecDss.readTimeSeries(file, "/A/B/FLOW/*/1Hour/F/");
        Instant start = Instant.parse("2020-01-01T00:00:00Z");
        Instant end = Instant.parse("2020-12-31T00:00:00Z");
        DssTimeSeries windowed = HecDss.readTimeSeries(file, "/A/B/FLOW/*/1Hour/F/", start, end);

        DssPairedData pd = HecDss.readPairedData(file, "/A/B/STAGE-FLOW///RATING/");
        DssGrid grid = HecDss.readGrid(file, "/grid/BASIN/PRECIP/01Jan2020/01Jan2020/SHG/");
        double[] array = HecDss.readArray(file, "/A/B/ARRAY///F/");
        String text = HecDss.readText(file, "/A/B/NOTE///F/");
        DssLocationInfo loc = HecDss.readLocationInfo(file, "/A/B/C//E/F/");
    }

    static void writeRecords(Path file) {
        DssTimeSeries ts = DssTimeSeries.of(
                new Instant[] { Instant.now() },
                new double[] { 1.0 },
                DssUnits.CFS,
                TimeSeriesDataType.INST_VAL,
                ZoneId.of("UTC"));
        HecDss.writeTimeSeries(file, "/A/B/FLOW/01Jan2020/1Hour/F/", ts);

        DssPairedData pd = DssPairedData.of(
                new double[] { 0, 1, 2 },
                new double[] { 0, 10, 20 },
                DssUnits.FEET, DssUnits.CFS,
                DssParameters.STAGE, DssParameters.FLOW);
        HecDss.writePairedData(file, "/A/B/STAGE-FLOW///RATING/", pd);

        DssGrid grid = DssGrid.of(
                new double[] { 0, 1, 2, 3 },
                2, 2, 1000.0, 0.0, 0.0,
                DssUnits.MM, DssCrs.SHG, GridDataType.PER_CUM);
        HecDss.writeGrid(file, "/grid/B/C/01Jan2020/01Jan2020/F/", grid);

        HecDss.writeArray(file, "/A/B/ARRAY///F/", new double[] { 1, 2, 3 });
        HecDss.writeText(file, "/A/B/NOTE///F/", "note");
        HecDss.writeLocationInfo(file, "/A/B/C//E/F/",
                DssLocationInfo.of(38.5, -121.5, 100.0, ZoneId.of("UTC")));
    }

    static void catalogAndMetadata(Path file) {
        List<DssCatalogEntry> all = HecDss.getCatalog(file);
        List<DssCatalogEntry> flow = HecDss.getCatalog(file, "/*/*/FLOW/*/*/*/");
        int count = HecDss.getRecordCount(file);
        DssRecordType type = HecDss.getRecordType(file, "/A/B/C/D/E/F/");
        boolean exists = HecDss.recordExists(file, "/A/B/C/D/E/F/");
        HecDss.deleteRecord(file, "/A/B/C/D/E/F/");
        HecDss.squeeze(file);
    }

    static void valueObjectAccessors() {
        DssTimeSeries ts = DssTimeSeries.of(new Instant[0], new double[0], DssUnits.CFS, TimeSeriesDataType.INST_VAL);
        int size = ts.size();
        double v = ts.size() == 0 ? Double.NaN : ts.value(0);
        Instant t = ts.size() == 0 ? Instant.EPOCH : ts.time(0);
        double[] values = ts.values();
        Instant[] times = ts.times();
        String units = ts.units();
        TimeSeriesDataType type = ts.type();
        ZoneId zone = ts.timeZone();
        boolean hasQuality = ts.hasQuality();
        int[] quality = ts.quality();
        DssTimeSeries clean = ts.dropMissing();
        boolean missing = ts.size() > 0 && ts.isMissing(0);

        DssGrid grid = DssGrid.of(new double[] {0, 0, 0, 0}, 2, 2, 1.0, 0.0, 0.0,
                DssUnits.MM, DssCrs.SHG, GridDataType.PER_CUM);
        double cell = grid.value(0, 0);
        double[] flat = grid.values();
        int w = grid.width();
        int h = grid.height();
        double cs = grid.cellSize();
        double xo = grid.xOrigin();
        double yo = grid.yOrigin();
        double x0 = grid.x(0);
        double y0 = grid.y(0);
        DssCrs crs = grid.crs();
        GridDataType gdt = grid.dataType();
        DssGrid withValues = grid.withValues(new double[] {1, 1, 1, 1}, DssUnits.MM, GridDataType.PER_AVER);
        DssGrid withUnits = grid.withUnits(DssUnits.CM);
        DssGrid withDataType = grid.withDataType(GridDataType.INST_VAL);

        DssPairedData pd = DssPairedData.of(new double[] {0}, new double[] {0},
                DssUnits.FEET, DssUnits.CFS, DssParameters.STAGE, DssParameters.FLOW);
        int ordinates = pd.ordinateCount();
        int curves = pd.curveCount();
        double[] ords = pd.ordinates();
        double[] curve0 = pd.curve(0);
        double[][] allCurves = pd.curves();
        double y = pd.yValue(0, 0);
        String[] labels = pd.labels();

        DssPathname parsed = DssPathname.parse("/A/B/C/D/E/F/").orElseThrow();
        String a = parsed.aPart();
        String partA = parsed.part(DssPathname.Part.A);
        DssPathname modified = parsed.with(DssPathname.Part.D, "*");
        boolean isPattern = parsed.isPattern();
        DssPathname pattern = parsed.toPattern(DssPathname.Part.D);
        boolean matches = parsed.matches(pattern);
    }

    static void errorHandling(Path file) {
        try {
            HecDss.readTimeSeries(file, "/A/B/C/D/E/F/");
        } catch (DssException e) {
            String msg = e.getMessage();
        }
    }

    static void intervalConstants() {
        String ePart = DssIntervals.HOUR_1;
        String day = DssIntervals.DAY_1;
        String irDay = DssIntervals.IR_DAY;
    }
}
