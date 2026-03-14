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

        CrsMapping.NativeCodes codes = CrsMapping.fromCrs(info.crs());

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        String timezoneStr = info.timeZone() != null ? info.timeZone().getId() : "";
        MemorySegment timezoneInput = arena.allocateFrom(timezoneStr);
        MemorySegment supplementalInput = arena.allocateFrom(info.description());

        int status = hecdss_h.hec_dss_locationStore(
                session.dssPointer(), pathnameInput,
                info.longitude(), info.latitude(), info.elevation(),
                codes.coordinateSystem(), codes.coordinateId(),
                codes.horizontalUnits(), codes.horizontalDatum(),
                codes.verticalUnits(), codes.verticalDatum(),
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
