package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssTimeSeries;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Instant;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TimeSeriesWriter {

    private TimeSeriesWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssTimeSeries data) {
        String ePart = pathname.ePart().toUpperCase();
        if (ePart.startsWith("IR-")) {
            writeIrregular(session, pathname, data);
        } else {
            writeRegular(session, pathname, data);
        }
    }

    private static void writeRegular(DssSession session, DssPathname pathname, DssTimeSeries data) {
        if (data.size() == 0) {
            throw new DssException("Cannot write empty time series to '%s'".formatted(pathname));
        }

        Arena arena = session.arena();

        Instant startInstant = data.time(0);
        NativeDateFormat time = NativeDateFormat.from(startInstant, startInstant);

        double[] values = data.values();
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(time.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(time.startTime());
        MemorySegment valueArray = NativeBuffers.allocateDoubles(arena, values);
        MemorySegment qualityArray = arena.allocate(C_INT, data.size());
        MemorySegment unitsInput = arena.allocateFrom(data.units());
        MemorySegment typeInput = arena.allocateFrom(data.type().dssString());
        String tz = data.timeZone() != null ? data.timeZone().getId() : "";
        MemorySegment timezoneInput = arena.allocateFrom(tz);

        int status = hecdss_h.hec_dss_tsStoreRegular(
                session.dssPointer(), pathnameInput,
                startDateInput, startTimeInput,
                valueArray, data.size(),
                qualityArray, 0,
                0, unitsInput, typeInput, timezoneInput, 0
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write regular time series '%s' to '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }
    }

    private static void writeIrregular(DssSession session, DssPathname pathname, DssTimeSeries data) {
        if (data.size() == 0) {
            throw new DssException("Cannot write empty time series to '%s'".formatted(pathname));
        }

        Arena arena = session.arena();
        int granularity = 60; // seconds per unit — minutes is the standard for irregular

        // Compute base date (julian days since DSS epoch) from first value
        long firstEpoch = data.time(0).getEpochSecond();
        long baseDaysSinceEpoch = (firstEpoch - InternalConstants.BASE_EPOCH_SECONDS) / 86400;
        long baseEpochSeconds = InternalConstants.BASE_EPOCH_SECONDS + baseDaysSinceEpoch * 86400;

        // Format the base date for native call
        Instant baseInstant = Instant.ofEpochSecond(baseEpochSeconds);
        NativeDateFormat baseDateFmt = NativeDateFormat.from(baseInstant, baseInstant);

        // Compute time offsets in granularity units from base
        int[] timeOffsets = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            timeOffsets[i] = (int) ((data.time(i).getEpochSecond() - baseEpochSeconds) / granularity);
        }

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment baseDateInput = arena.allocateFrom(baseDateFmt.startDate());
        MemorySegment timesInput = NativeBuffers.allocateInts(arena, timeOffsets);
        MemorySegment valueArray = NativeBuffers.allocateDoubles(arena, data.values());
        MemorySegment qualityArray = arena.allocate(C_INT, data.size());
        MemorySegment unitsInput = arena.allocateFrom(data.units());
        MemorySegment typeInput = arena.allocateFrom(data.type().dssString());
        String tz = data.timeZone() != null ? data.timeZone().getId() : "";
        MemorySegment timezoneInput = arena.allocateFrom(tz);

        int status = hecdss_h.hec_dss_tsStoreIregular(
                session.dssPointer(), pathnameInput,
                baseDateInput, timesInput, granularity,
                valueArray, data.size(),
                qualityArray, 0,
                0, unitsInput, typeInput, timezoneInput, 0
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write irregular time series '%s' to '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }
    }
}
