package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class HecDssLocationInfoWriteTest {

    @Test
    void writeAndReadLocationInfo() {
        Path dssFile = TestUtil.createTempFile("location-test.dss");
        String pathname = "/TEST/LOCATION/DATA///LOC-TEST/";

        DssLocationInfo input = DssLocationInfo.of(38.5, -121.5, 100.0, ZoneId.of("UTC"));

        HecDss.writeLocationInfo(dssFile, pathname, input);

        DssLocationInfo output = HecDss.readLocationInfo(dssFile, pathname);
        assertEquals(-121.5, output.longitude(), 0.001);
        assertEquals(38.5, output.latitude(), 0.001);
        assertEquals(100.0, output.elevation(), 0.001);
    }

    @Test
    void readLocationInfoWriteToNewPathRead() {
        Path dssFile = TestUtil.copyResourceToTemp("sample7.dss");
        String readPath = "/MISSISSIPPI/ST. LOUIS/Location Info////";
        String writePath = "/MISSISSIPPI/ST. LOUIS/Location Info///newPath/";

        DssLocationInfo original = HecDss.readLocationInfo(dssFile, readPath);
        HecDss.writeLocationInfo(dssFile, writePath, original);

        DssLocationInfo reread = HecDss.readLocationInfo(dssFile, writePath);
        assertEquals(original.longitude(), reread.longitude(), 0.001);
        assertEquals(original.latitude(), reread.latitude(), 0.001);
        assertEquals(original.elevation(), reread.elevation(), 0.001);
        assertEquals(original.crs(), reread.crs());
        assertEquals(original.timeZone(), reread.timeZone());
        assertEquals(original.description(), reread.description());
    }

    @Test
    void readModifyWriteReadLocationInfo() {
        Path dssFile = TestUtil.copyResourceToTemp("sample7.dss");
        String readPath = "/MISSISSIPPI/ST. LOUIS/Location Info////";
        String writePath = "/MISSISSIPPI/ST. LOUIS/Location Info///modified/";

        DssLocationInfo original = HecDss.readLocationInfo(dssFile, readPath);

        DssLocationInfo modified = new DssLocationInfo(
                original.longitude() + 1.0, original.latitude(), original.elevation(),
                original.crs(),
                original.timeZone(), original.description()
        );
        HecDss.writeLocationInfo(dssFile, writePath, modified);

        DssLocationInfo reread = HecDss.readLocationInfo(dssFile, writePath);
        assertEquals(original.longitude() + 1.0, reread.longitude(), 0.001);
    }
}
