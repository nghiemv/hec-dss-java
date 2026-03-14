package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssLocationInfoWriteTest {

    @Test
    void writeAndReadLocationInfo() {
        Path dssFile = TestUtil.createTempFile("location-test.dss");
        String pathname = "/TEST/LOCATION/DATA///LOC-TEST/";

        DssLocationInfo input = DssLocationInfo.of(38.5, -121.5, 100.0, "UTC");

        HecDss.writeLocationInfo(dssFile, pathname, input);

        DssLocationInfo output = HecDss.readLocationInfo(dssFile, pathname);
        assertEquals(-121.5, output.x(), 0.001);
        assertEquals(38.5, output.y(), 0.001);
        assertEquals(100.0, output.z(), 0.001);
    }

    @Test
    void readLocationInfoWriteToNewPathRead() {
        Path dssFile = TestUtil.copyResourceToTemp("sample7.dss");
        String readPath = "/MISSISSIPPI/ST. LOUIS/Location Info////";
        String writePath = "/MISSISSIPPI/ST. LOUIS/Location Info///newPath/";

        DssLocationInfo original = HecDss.readLocationInfo(dssFile, readPath);
        HecDss.writeLocationInfo(dssFile, writePath, original);

        DssLocationInfo reread = HecDss.readLocationInfo(dssFile, writePath);
        assertEquals(original.x(), reread.x(), 0.001);
        assertEquals(original.y(), reread.y(), 0.001);
        assertEquals(original.z(), reread.z(), 0.001);
        assertEquals(original.crs(), reread.crs());
        assertEquals(original.timeZoneName(), reread.timeZoneName());
        assertEquals(original.supplemental(), reread.supplemental());
    }

    @Test
    void readModifyWriteReadLocationInfo() {
        Path dssFile = TestUtil.copyResourceToTemp("sample7.dss");
        String readPath = "/MISSISSIPPI/ST. LOUIS/Location Info////";
        String writePath = "/MISSISSIPPI/ST. LOUIS/Location Info///modified/";

        DssLocationInfo original = HecDss.readLocationInfo(dssFile, readPath);

        DssLocationInfo modified = new DssLocationInfo(
                original.x() + 1.0, original.y(), original.z(),
                original.crs(),
                original.timeZoneName(), original.supplemental()
        );
        HecDss.writeLocationInfo(dssFile, writePath, modified);

        DssLocationInfo reread = HecDss.readLocationInfo(dssFile, writePath);
        assertEquals(original.x() + 1.0, reread.x(), 0.001);
    }
}
