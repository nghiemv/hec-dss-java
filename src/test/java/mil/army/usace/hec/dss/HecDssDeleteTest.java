package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HecDssDeleteTest {

    @Test
    void deleteRecordVerifyGone() {
        Path dssFile = TestUtil.copyResourceToTemp("sample7.dss");
        String pathname = "//SACRAMENTO/PRECIP-INC//1Day/OBS/";

        Instant t1 = ZonedDateTime.parse("2005-01-01T00:00:00Z").toInstant();
        Instant t2 = ZonedDateTime.parse("2005-01-04T00:00:00Z").toInstant();
        DssTimeSeries ts = HecDss.readTimeSeries(dssFile, pathname, t1, t2);
        assertTrue(ts.size() > 0);

        String newPath = "//SACRAMENTO/PRECIP-INC//1Day/OBS-to-delete/";
        HecDss.writeTimeSeries(dssFile, newPath, ts);

        DssTimeSeries written = HecDss.readTimeSeries(dssFile, newPath, t1, t2);
        assertEquals(ts.size(), written.size());

        var catalog = HecDss.getCatalog(dssFile);
        for (DssCatalogEntry entry : catalog) {
            if (entry.pathname().fPart().equals("OBS-to-delete")) {
                HecDss.delete(dssFile, entry.pathname().toString());
            }
        }

        assertThrows(DssException.class, () ->
                HecDss.readTimeSeries(dssFile, newPath, t1, t2));
    }

    @Test
    void deleteTextRecord() {
        Path dssFile = TestUtil.createTempFile("delete-text-test.dss");
        String pathname = "/TEST/LOCATION/NOTE//TEXT/TO-DELETE/";

        HecDss.writeText(dssFile, pathname, "This will be deleted");
        assertTrue(HecDss.getRecordCount(dssFile) > 0);

        HecDss.delete(dssFile, pathname);
    }
}
