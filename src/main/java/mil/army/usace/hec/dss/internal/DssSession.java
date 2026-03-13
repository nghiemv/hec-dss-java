package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.logging.Logger;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

final class DssSession implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(DssSession.class.getName());

    private final String dssFilePath;
    private final Arena memorySession;
    private final MemorySegment dssStackPointer;

    static {
        NativeLibrary.HEC_DSS.initialize();
    }

    private DssSession(String dssFilePath) {
        this.dssFilePath = dssFilePath;
        this.memorySession = Arena.ofConfined();
        this.dssStackPointer = initPointer(dssFilePath);
    }

    public static DssSession initiate(String dssFilePath) {
        return new DssSession(dssFilePath);
    }

    public Arena getMemorySession() {
        return this.memorySession;
    }

    public MemorySegment getDssStackPointer() {
        return this.dssStackPointer;
    }

    private static MemorySegment initPointer(String dssFilePath) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment pathHolder = arena.allocateFrom(dssFilePath);
            MemorySegment pointerHolder = arena.allocate(C_POINTER);
            int openStatus = hecdss_h.hec_dss_open(pathHolder, pointerHolder);

            if (openStatus != 0) {
                throw new DssException("Failed to open DSS file: " + dssFilePath);
            }
            return pointerHolder.get(ValueLayout.ADDRESS, 0);
        }
    }

    @Override
    public void close() {
        int closeStatus = hecdss_h.hec_dss_close(this.dssStackPointer);
        if (closeStatus != 0) {
            logger.severe("Failed to close DSS File: " + this.dssFilePath);
        }
        this.memorySession.close();
    }
}
