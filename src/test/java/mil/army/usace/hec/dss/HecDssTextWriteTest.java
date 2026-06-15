package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssTextWriteTest {

    @Test
    void writeAndReadText() {
        Path dssFile = TestUtil.createTempFile("text-test.dss");
        String pathname = "/TEST/LOCATION/NOTE//TEXT/VERSION/";

        String text = "Hello, DSS! This is a test text record.";
        HecDss.writeText(dssFile, pathname, text);

        String output = HecDss.readText(dssFile, pathname);
        assertEquals(text, output);
    }

    @Test
    void writeAndReadMultilineText() {
        Path dssFile = TestUtil.createTempFile("text-multiline-test.dss");
        String pathname = "/A/B/C/D/E/F/";

        String text = "This is a test\nof text data\nin a DSS file.\n";
        HecDss.writeText(dssFile, pathname, text);

        String output = HecDss.readText(dssFile, pathname);
        assertEquals(text, output);
    }
}
