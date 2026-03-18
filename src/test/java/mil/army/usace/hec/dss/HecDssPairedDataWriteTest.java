package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssPairedDataWriteTest {

    @Test
    void writeAndReadPairedData() {
        Path dssFile = TestUtil.createTempFile("paired-test.dss");
        String pathname = "/TEST/LOCATION/STAGE-FLOW///RATING/";

        double[] ordinates = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] values = {0.0, 10.0, 50.0, 150.0, 300.0};
        DssPairedData input = DssPairedData.of(ordinates, values,
                "FEET", "CFS", "Stage", "Flow");

        HecDss.writePairedData(dssFile, pathname, input);

        DssPairedData output = HecDss.readPairedData(dssFile, pathname);
        assertEquals(5, output.numberOrdinates());
        assertEquals(1, output.numberCurves());
        assertArrayEquals(ordinates, output.ordinates());
        assertArrayEquals(values, output.curve(0));
        assertEquals("FEET", output.xUnits());
        assertEquals("CFS", output.yUnits());
    }

    @Test
    void readPairedDataWriteToNewPathRead() {
        Path dssFile = TestUtil.copyResourceToTemp("sample7.dss");
        String readPath = "/MY BASIN/DEER CREEK/STAGE-FLOW///USGS/";
        String writePath = "/MY BASIN/DEER CREEK/STAGE-FLOW///USGS-copy/";

        DssPairedData original = HecDss.readPairedData(dssFile, readPath);

        HecDss.writePairedData(dssFile, writePath, original);

        DssPairedData reread = HecDss.readPairedData(dssFile, writePath);
        assertArrayEquals(original.ordinates(), reread.ordinates());
        assertArrayEquals(original.curve(0), reread.curve(0));
        assertEquals(original.xUnits(), reread.xUnits());
        assertEquals(original.yUnits(), reread.yUnits());
    }

    @Test
    void writeMultiCurvePairedDataThenRead() {
        Path dssFile = TestUtil.createTempFile("multi-curve-test.dss");
        String pathname = "/TEST/LOCATION/STAGE-FLOW///MULTI-CURVE/";

        double[] ordinates = {0.0, 11.0, 22.0, 33.0, 44.0};
        double[][] curves = new double[3][ordinates.length];
        for (int c = 0; c < 3; c++) {
            for (int i = 0; i < ordinates.length; i++) {
                curves[c][i] = ordinates[i] + c;
            }
        }
        String[] labels = {"x plus 0", "x plus 1", "x plus 2"};
        DssPairedData input = new DssPairedData(
                ordinates, curves, labels,
                "cm", "CFS", "Stage", "Flow"
        );

        HecDss.writePairedData(dssFile, pathname, input);

        DssPairedData output = HecDss.readPairedData(dssFile, pathname);
        assertEquals(5, output.numberOrdinates());
        assertEquals(3, output.numberCurves());
        assertArrayEquals(ordinates, output.ordinates());
        for (int c = 0; c < 3; c++) {
            assertArrayEquals(curves[c], output.curve(c));
        }
    }

    @Test
    void readMultiCurvePairedDataModifyLabelsRoundTrip() {
        Path dssFile = TestUtil.copyResourceToTemp("R703F3-PF_v7.dss");
        String readPath = "/FOLSOM/AUXILIARY SPILLWAY-GATE RATING/ELEV-FLOW/PAIREDVALUESEXT///";
        String writePath = "/FOLSOM/AUXILIARY SPILLWAY-GATE RATING/ELEV-FLOW/PAIREDVALUESEXT//label-test/";

        DssPairedData original = HecDss.readPairedData(dssFile, readPath);

        double[][] curves = new double[original.numberCurves()][];
        for (int c = 0; c < original.numberCurves(); c++) {
            curves[c] = original.curve(c);
        }

        String[] newLabels = original.labels().clone();
        newLabels[Math.min(3, newLabels.length - 1)] = "New Label";
        DssPairedData modified = new DssPairedData(
                original.ordinates(), curves,
                newLabels, original.xUnits(), original.yUnits(),
                original.xType(), original.yType()
        );

        HecDss.writePairedData(dssFile, writePath, modified);

        DssPairedData reread = HecDss.readPairedData(dssFile, writePath);
        assertArrayEquals(original.ordinates(), reread.ordinates());
        for (int c = 0; c < original.numberCurves(); c++) {
            assertArrayEquals(original.curve(c), reread.curve(c));
        }
        assertEquals("New Label", reread.labels()[Math.min(3, reread.labels().length - 1)]);
    }
}
