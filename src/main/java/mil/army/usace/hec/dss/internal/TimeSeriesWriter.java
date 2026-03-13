package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssTimeSeries;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TimeSeriesWriter {
    private static final long BASE_EPOCH_SECONDS =
            OffsetDateTime.of(1899, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC).toEpochSecond();

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

        Instant startInstant = Instant.ofEpochSecond(data.epochSeconds()[0]);
        NativeDateFormat time = NativeDateFormat.from(startInstant, startInstant);

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(time.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(time.startTime());
        MemorySegment valueArray = allocateDoubles(arena, data.values());
        MemorySegment qualityArray = arena.allocate(C_INT, data.size());
        MemorySegment unitsInput = arena.allocateFrom(data.units());
        MemorySegment typeInput = arena.allocateFrom(data.type());
        MemorySegment timezoneInput = arena.allocateFrom("");

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
        long firstEpoch = data.epochSeconds()[0];
        long baseDaysSinceEpoch = (firstEpoch - BASE_EPOCH_SECONDS) / 86400;
        long baseEpochSeconds = BASE_EPOCH_SECONDS + baseDaysSinceEpoch * 86400;

        // Format the base date for native call
        Instant baseInstant = Instant.ofEpochSecond(baseEpochSeconds);
        NativeDateFormat baseDateFmt = NativeDateFormat.from(baseInstant, baseInstant);

        // Compute time offsets in granularity units from base
        int[] timeOffsets = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            timeOffsets[i] = (int) ((data.epochSeconds()[i] - baseEpochSeconds) / granularity);
        }

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment baseDateInput = arena.allocateFrom(baseDateFmt.startDate());
        MemorySegment timesInput = allocateInts(arena, timeOffsets);
        MemorySegment valueArray = allocateDoubles(arena, data.values());
        MemorySegment qualityArray = arena.allocate(C_INT, data.size());
        MemorySegment unitsInput = arena.allocateFrom(data.units());
        MemorySegment typeInput = arena.allocateFrom(data.type());
        MemorySegment timezoneInput = arena.allocateFrom("");

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

    private static MemorySegment allocateDoubles(Arena arena, double[] values) {
        MemorySegment segment = arena.allocate(C_DOUBLE, values.length);
        MemorySegment.copy(values, 0, segment, C_DOUBLE, 0, values.length);
        return segment;
    }

    private static MemorySegment allocateInts(Arena arena, int[] values) {
        MemorySegment segment = arena.allocate(C_INT, values.length);
        MemorySegment.copy(values, 0, segment, C_INT, 0, values.length);
        return segment;
    }
}
