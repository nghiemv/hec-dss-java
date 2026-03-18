package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.MemorySegment;

public final class DeleteOperation {
    private DeleteOperation() {}

    public static void delete(DssSession session, DssPathname pathname) {
        MemorySegment pathnameInput = session.arena().allocateFrom(pathname.toString());
        int status = hecdss_h.hec_dss_delete(session.dssPointer(), pathnameInput);
        if (status != 0) {
            throw new DssException(
                    "Failed to delete '%s' from '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }
    }
}
