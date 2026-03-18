package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssTimeSeries;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TimeSeriesWriter {

    private TimeSeriesWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssTimeSeries data) {
        String ePart = pathname.ePart().toUpperCase(java.util.Locale.ROOT);
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

        ZoneId zone = DssTimeZone.zoneOrUtc(data.timeZone());
        Instant startInstant = data.time(0);
        NativeDateFormat time = NativeDateFormat.from(startInstant, startInstant, zone);

        double[] values = data.values();
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(time.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(time.startTime());
        MemorySegment valueArray = NativeBuffers.allocateDoubles(arena, values);
        int[] qFlags = data.qualityFlags();
        MemorySegment qualityArray = qFlags != null
                ? NativeBuffers.allocateInts(arena, qFlags)
                : arena.allocate(C_INT, data.size());
        int qualitySize = qFlags != null ? 1 : 0;
        MemorySegment unitsInput = arena.allocateFrom(data.units());
        MemorySegment typeInput = arena.allocateFrom(data.type().dssString());
        MemorySegment timezoneInput = arena.allocateFrom(DssTimeZone.toDssString(data.timeZone()));

        int status = hecdss_h.hec_dss_tsStoreRegular(
                session.dssPointer(), pathnameInput,
                startDateInput, startTimeInput,
                valueArray, data.size(),
                qualityArray, qualitySize,
                0, unitsInput, typeInput, timezoneInput, 0
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write regular time series '%s' to '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }
    }

    private static void writeIrregular(DssSession session, DssPathname pathname, DssTimeSeries data) {
        if (data.size() == 0) {
            throw new DssException("Cannot write empty time series to '%s'".formatted(pathname));
        }

        Arena arena = session.arena();
        ZoneId zone = DssTimeZone.zoneOrUtc(data.timeZone());
        int granularity = 60; // seconds per unit — minutes is the standard for irregular

        // Convert first Instant to local time in the target timezone, then compute
        // base date and time offsets in that timezone's local calendar
        ZonedDateTime firstLocal = data.time(0).atZone(zone);
        ZonedDateTime baseLocal = firstLocal.toLocalDate()
                .atStartOfDay(zone);
        long baseEpochSeconds = baseLocal.toEpochSecond();

        // Format the base date for native call
        NativeDateFormat baseDateFmt = NativeDateFormat.from(
                baseLocal.toInstant(), baseLocal.toInstant(), zone);

        // Compute time offsets in granularity units from base, in local time
        int[] timeOffsets = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            long localSeconds = data.time(i).atZone(zone).toEpochSecond();
            timeOffsets[i] = (int) ((localSeconds - baseEpochSeconds) / granularity);
        }

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment baseDateInput = arena.allocateFrom(baseDateFmt.startDate());
        MemorySegment timesInput = NativeBuffers.allocateInts(arena, timeOffsets);
        MemorySegment valueArray = NativeBuffers.allocateDoubles(arena, data.values());
        int[] qFlags = data.qualityFlags();
        MemorySegment qualityArray = qFlags != null
                ? NativeBuffers.allocateInts(arena, qFlags)
                : arena.allocate(C_INT, data.size());
        int qualitySize = qFlags != null ? 1 : 0;
        MemorySegment unitsInput = arena.allocateFrom(data.units());
        MemorySegment typeInput = arena.allocateFrom(data.type().dssString());
        MemorySegment timezoneInput = arena.allocateFrom(DssTimeZone.toDssString(data.timeZone()));

        int status = hecdss_h.hec_dss_tsStoreIregular(
                session.dssPointer(), pathnameInput,
                baseDateInput, timesInput, granularity,
                valueArray, data.size(),
                qualityArray, qualitySize,
                0, unitsInput, typeInput, timezoneInput, 0
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write irregular time series '%s' to '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }
    }
}
