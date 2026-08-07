package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssMessageLevel;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Path;

/**
 * Controls where the native library writes its diagnostics and how much of it
 * it writes. Both settings are global to the process.
 */
public final class MessageControl {
    private MessageControl() {}

    public static void setLevel(DssMessageLevel level) {
        NativeLibrary.load();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment name = arena.allocateFrom("mlvl");
            int status = hecdss_h.hec_dss_set_value(name, legacyCode(level));
            if (status != 0) {
                throw new DssException(
                        "Failed to set DSS message level to %s: %s"
                                .formatted(level, NativeStatusCode.describe(status)));
            }
        }
    }

    public static void setLogFile(Path file) {
        NativeLibrary.load();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment name = arena.allocateFrom(file.toAbsolutePath().toString());
            int status = hecdss_h.hec_dss_open_log_file(name);
            if (status != 0) {
                throw new DssException(
                        "Failed to open DSS log file '%s': %s"
                                .formatted(file, NativeStatusCode.describe(status)));
            }
        }
    }

    public static void logToConsole() {
        NativeLibrary.load();
        hecdss_h.hec_dss_close_log_file.makeInvoker().apply();
    }

    /**
     * Translates a {@code MESS_LEVEL_*} constant into the argument {@code zset}
     * expects.
     *
     * <p>{@code zset("mlvl", n)} does not take a {@code MESS_LEVEL_*} value. It
     * takes a number on the wider legacy DSS-6 scale and buckets it down onto
     * {@code MESS_LEVEL_*} before applying it (heclib {@code zset.c}), so the
     * two scales agree only at 0, 1, and 2. Passing a {@code MESS_LEVEL_*}
     * value straight through would silently land a level too low: 3 (GENERAL)
     * buckets to TERSE, 5 (INTERNAL_DIAGNOSTIC_1) buckets to USER_DIAG.
     */
    private static int legacyCode(DssMessageLevel level) {
        return switch (level) {
            case NONE -> 0;
            case CRITICAL -> 1;
            case TERSE -> 2;
            case GENERAL -> 4;
            case USER_DIAGNOSTIC -> 5;
            case INTERNAL_DIAGNOSTIC_1 -> 11;
            case INTERNAL_DIAGNOSTIC_2 -> 13;
        };
    }
}
