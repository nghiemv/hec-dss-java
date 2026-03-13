package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssLocationInfo;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class LocationInfoReader {
    private static final int STRING_BUFFER_LENGTH = NativeBuffers.STRING_BUFFER_LENGTH;
    private static final int SUPPLEMENTAL_BUFFER_LENGTH = 4096;

    private LocationInfoReader() {}

    public static DssLocationInfo read(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment xOutput = arena.allocate(C_DOUBLE, 1);
        MemorySegment yOutput = arena.allocate(C_DOUBLE, 1);
        MemorySegment zOutput = arena.allocate(C_DOUBLE, 1);
        MemorySegment coordinateSystemOutput = arena.allocate(C_INT, 1);
        MemorySegment coordinateIdOutput = arena.allocate(C_INT, 1);
        MemorySegment horizontalUnitsOutput = arena.allocate(C_INT, 1);
        MemorySegment horizontalDatumOutput = arena.allocate(C_INT, 1);
        MemorySegment verticalUnitsOutput = arena.allocate(C_INT, 1);
        MemorySegment verticalDatumOutput = arena.allocate(C_INT, 1);
        MemorySegment timezoneOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment supplementalOutput = arena.allocate(C_CHAR, SUPPLEMENTAL_BUFFER_LENGTH);

        int status = hecdss_h.hec_dss_locationRetrieve(
                session.dssPointer(), pathnameInput,
                xOutput, yOutput, zOutput,
                coordinateSystemOutput, coordinateIdOutput,
                horizontalUnitsOutput, horizontalDatumOutput,
                verticalUnitsOutput, verticalDatumOutput,
                timezoneOutput, STRING_BUFFER_LENGTH,
                supplementalOutput, SUPPLEMENTAL_BUFFER_LENGTH
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to retrieve location info '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        String crs = CrsMapping.toCrs(
                coordinateSystemOutput.get(C_INT, 0),
                coordinateIdOutput.get(C_INT, 0),
                horizontalUnitsOutput.get(C_INT, 0),
                horizontalDatumOutput.get(C_INT, 0),
                verticalUnitsOutput.get(C_INT, 0),
                verticalDatumOutput.get(C_INT, 0)
        );

        return new DssLocationInfo(
                xOutput.get(C_DOUBLE, 0),
                yOutput.get(C_DOUBLE, 0),
                zOutput.get(C_DOUBLE, 0),
                crs,
                timezoneOutput.getString(0),
                supplementalOutput.getString(0)
        );
    }
}
