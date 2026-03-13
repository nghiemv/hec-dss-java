package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssLocationInfo;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class LocationInfoWriter {
    private LocationInfoWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssLocationInfo info) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment timezoneInput = arena.allocateFrom(info.timeZoneName());
        MemorySegment supplementalInput = arena.allocateFrom(info.supplemental());

        int status = hecdss_h.hec_dss_locationStore(
                session.dssPointer(), pathnameInput,
                info.x(), info.y(), info.z(),
                info.coordinateSystem().code(), info.coordinateId(),
                info.horizontalUnits().code(), info.horizontalDatum().code(),
                info.verticalUnits().code(), info.verticalDatum().code(),
                timezoneInput, supplementalInput,
                1  // replace = true
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write location info '%s' to '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }
    }
}
