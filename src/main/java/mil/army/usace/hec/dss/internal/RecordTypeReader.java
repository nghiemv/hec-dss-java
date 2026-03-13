package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssRecordType;

import java.lang.foreign.MemorySegment;

public final class RecordTypeReader {
    private RecordTypeReader() {}

    public static DssRecordType read(DssSession session, DssPathname pathname) {
        MemorySegment pathnameInput = session.arena().allocateFrom(pathname.toString());
        int nativeCode = hecdss_h.hec_dss_recordType(session.dssPointer(), pathnameInput);
        return DssRecordType.fromNativeCode(nativeCode);
    }
}
