package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssRecordType;

import java.lang.foreign.MemorySegment;

public final class RecordTypeReader {
    private RecordTypeReader() {}

    public static DssRecordType read(DssSession session, DssPathname pathname) {
        MemorySegment pathnameInput = session.arena().allocateFrom(pathname.toString());
        int code = hecdss_h.hec_dss_recordType(session.dssPointer(), pathnameInput);
        return fromNativeCode(code);
    }

    private static DssRecordType fromNativeCode(int code) {
        if (code >= 90 && code <= 93) return DssRecordType.ARRAY;
        if (code >= 100 && code < 110) return DssRecordType.REGULAR_TIME_SERIES;
        if (code >= 110 && code < 200) return DssRecordType.IRREGULAR_TIME_SERIES;
        if (code >= 200 && code < 300) return DssRecordType.PAIRED_DATA;
        if (code >= 300 && code < 400) return DssRecordType.TEXT;
        if (code >= 400 && code < 450) return DssRecordType.GRID;
        if (code == 450) return DssRecordType.TIN;
        if (code == 20) return DssRecordType.LOCATION_INFO;
        return DssRecordType.UNKNOWN;
    }
}
