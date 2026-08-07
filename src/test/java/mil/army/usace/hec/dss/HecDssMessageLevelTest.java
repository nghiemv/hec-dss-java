package mil.army.usace.hec.dss;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssMessageLevelTest {

    private static final String PATHNAME =
            "/regular-time-series/GAPT/FLOW/01Sep2021/6Hour/forecast1/";

    /**
     * The message level and log destination are process-global, so every test
     * here has to hand the JVM back the way it found it or it changes what the
     * rest of the suite prints.
     */
    @AfterEach
    void restoreDefaults() {
        HecDss.logToConsole();
        HecDss.setMessageLevel(DssMessageLevel.GENERAL);
    }

    @Test
    void generalLevelLogsFileOpenAndClose() throws IOException {
        String log = captureAt(DssMessageLevel.GENERAL);
        assertTrue(log.contains("zopen"), () -> "expected an open record in: " + log);
        assertTrue(log.contains("zclose"), () -> "expected a close record in: " + log);
    }

    @Test
    void criticalLevelSuppressesFileOpenAndClose() throws IOException {
        String log = captureAt(DssMessageLevel.CRITICAL);
        assertFalse(log.contains("zopen"), () -> "expected no open record in: " + log);
        assertFalse(log.contains("zclose"), () -> "expected no close record in: " + log);
    }

    /**
     * GENERAL is 3 on heclib's {@code MESS_LEVEL_*} scale but has to be passed
     * to {@code zset} as 4; passing 3 lands on TERSE. Read statistics appear at
     * GENERAL and not at TERSE, which is what separates the two here.
     */
    @Test
    void generalLevelIsNotSilentlyDowngradedToTerse() throws IOException {
        String terse = captureAt(DssMessageLevel.TERSE);
        String general = captureAt(DssMessageLevel.GENERAL);
        assertTrue(general.length() > terse.length(),
                () -> "GENERAL (%d chars) should say more than TERSE (%d chars)"
                        .formatted(general.length(), terse.length()));
    }

    @Test
    void everyLevelIsAccepted() {
        for (DssMessageLevel level : DssMessageLevel.values()) {
            assertDoesNotThrow(() -> HecDss.setMessageLevel(level), level::name);
        }
        // Leaves the JVM at INTERNAL_DIAGNOSTIC_2, which makes the restore in
        // @AfterEach announce itself on stdout. Step down first.
        HecDss.setMessageLevel(DssMessageLevel.CRITICAL);
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> HecDss.setMessageLevel(null));
        assertThrows(NullPointerException.class, () -> HecDss.setLogFile(null));
    }

    /** Runs one read with output redirected to a file, and returns what was written. */
    private String captureAt(DssMessageLevel level) throws IOException {
        Path logFile = TestUtil.createTempFile("dss-messages.log");
        HecDss.setMessageLevel(level);
        HecDss.setLogFile(logFile);
        try {
            HecDss.readTimeSeries(TestUtil.getResourceFile("examples-all-data-types.dss"), PATHNAME);
        } finally {
            // zcloseLog flushes; without it the file may still be empty.
            HecDss.logToConsole();
        }
        return Files.readString(logFile);
    }
}
