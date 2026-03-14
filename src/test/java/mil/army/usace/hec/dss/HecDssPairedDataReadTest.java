package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssPairedDataReadTest {

    @Test
    void readPairedData() {
        Path dssFile = TestUtil.getResourceFile("sample7.dss");
        String pathname = "/MY BASIN/DEER CREEK/STAGE-FLOW///USGS/";

        DssPairedData pd = HecDss.readPairedData(dssFile, pathname);
        assertTrue(pd.numberOrdinates() > 0);
        assertEquals(1, pd.numberCurves());
        assertEquals("FEET", pd.xUnits());
        assertEquals("CFS", pd.yUnits());
    }

    @Test
    void readMultiCurvePairedData() {
        Path dssFile = TestUtil.getResourceFile("R703F3-PF_v7.dss");
        String pathname = "/FOLSOM/AUXILIARY SPILLWAY-GATE RATING/ELEV-FLOW/PAIREDVALUESEXT///";

        DssPairedData pd = HecDss.readPairedData(dssFile, pathname);
        assertTrue(pd.numberOrdinates() > 0);
        assertTrue(pd.numberCurves() > 1, "Expected multi-curve paired data");
        assertTrue(pd.labels().length > 0, "Expected labels on multi-curve data");
    }
}
