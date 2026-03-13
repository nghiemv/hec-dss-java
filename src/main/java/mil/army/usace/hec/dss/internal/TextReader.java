package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TextReader {
    private static final int INITIAL_BUFFER_SIZE = 4096;
    private static final int MAX_BUFFER_SIZE = 2 * 1024 * 1024;

    private TextReader() {}

    public static String read(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        // Try with initial buffer, grow if needed
        int bufferSize = INITIAL_BUFFER_SIZE;
        while (bufferSize <= MAX_BUFFER_SIZE) {
            MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
            MemorySegment buffer = arena.allocate(C_CHAR, bufferSize);

            int status = hecdss_h.hec_dss_textRetrieve(
                    session.dssPointer(), pathnameInput,
                    buffer, bufferSize
            );

            if (status == 0) {
                return buffer.getString(0);
            }

            // Status might indicate buffer too small — try larger
            bufferSize *= 4;
        }

        throw new DssException(
                "Failed to retrieve text '%s' from '%s': text exceeds maximum buffer size"
                        .formatted(pathname, session.filePath()));
    }
}
