package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class SqueezeOperation {
    private SqueezeOperation() {}

    public static void squeeze(String filename) {
        NativeLibrary.load();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment filenameInput = arena.allocateFrom(filename);
            int status = hecdss_h.hec_dss_squeeze(filenameInput);
            if (status != 0) {
                throw new DssException(
                        "Failed to squeeze '%s': native status code %d"
                                .formatted(filename, status));
            }
        }
    }
}
