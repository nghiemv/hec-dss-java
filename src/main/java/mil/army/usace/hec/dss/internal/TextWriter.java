package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TextWriter {
    private TextWriter() {}

    public static void write(DssSession session, DssPathname pathname, String text) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment textInput = arena.allocateFrom(text);

        int status = hecdss_h.hec_dss_textStore(
                session.dssPointer(), pathnameInput,
                textInput, (int) textInput.byteSize() - 1  // exclude null terminator
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write text '%s' to '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }
    }
}
