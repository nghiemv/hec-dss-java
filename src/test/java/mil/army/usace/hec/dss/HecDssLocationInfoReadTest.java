package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssLocationInfoReadTest {

    @Test
    void readLocationInfo() {
        Path dssFile = TestUtil.getResourceFile("sample7.dss");
        String pathname = "/MISSISSIPPI/ST. LOUIS/Location Info////";

        DssLocationInfo loc = HecDss.readLocationInfo(dssFile, pathname);
        assertNotNull(loc.timeZoneName());
    }
}
