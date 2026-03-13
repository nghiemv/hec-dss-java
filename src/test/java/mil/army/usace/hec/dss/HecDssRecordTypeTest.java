package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssRecordTypeTest {
    @Test
    void detectRegularTimeSeries() {
        Path dssFile = TestUtil.getResourceFile("examples-all-data-types.dss");
        String pathname = "/regular-time-series/GAPT/FLOW/01Sep2021/6Hour/forecast1/";
        assertEquals(DssRecordType.REGULAR_TIME_SERIES, HecDss.getRecordType(dssFile, pathname));
    }

    @Test
    void detectIrregularTimeSeries() {
        Path dssFile = TestUtil.getResourceFile("examples-all-data-types.dss");
        String pathname = "/irregular-time-series/FAIR OAKS CA/FLOW-ANNUAL PEAK/01Jan1900/IR-Century/USGS/";
        assertEquals(DssRecordType.IRREGULAR_TIME_SERIES, HecDss.getRecordType(dssFile, pathname));
    }
}
