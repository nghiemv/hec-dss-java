package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TimeSeriesReader {
    private static final int STRING_BUFFER_LENGTH = NativeBuffers.STRING_BUFFER_LENGTH;

    private TimeSeriesReader() {}

    public static DssTimeSeries read(DssSession session, DssPathname pathname) {
        Instant[] range = readRange(session, pathname);
        return read(session, pathname, range[0], range[1]);
    }

    public static DssTimeSeries read(DssSession session, DssPathname pathname,
                                     Instant start, Instant end) {
        Arena arena = session.arena();
        NativeDateFormat time = NativeDateFormat.from(start, end);

        // Allocate date/time strings once — reused for both sizing and retrieval calls
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(time.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(time.startTime());
        MemorySegment endDateInput = arena.allocateFrom(time.endDate());
        MemorySegment endTimeInput = arena.allocateFrom(time.endTime());

        // Get sizes
        MemorySegment numberValuesOutput = arena.allocate(C_INT, 1);
        MemorySegment qualityWidthOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_tsGetSizes(
                session.dssPointer(), pathnameInput,
                startDateInput, startTimeInput, endDateInput, endTimeInput,
                numberValuesOutput, qualityWidthOutput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to get time series sizes for '%s' from '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }

        int numberValues = numberValuesOutput.get(C_INT, 0);
        int qualityWidth = qualityWidthOutput.get(C_INT, 0);

        // Retrieve data
        MemorySegment timeArrayOutput = arena.allocate(C_INT, numberValues);
        MemorySegment valueArrayOutput = arena.allocate(C_DOUBLE, numberValues);
        MemorySegment numberValuesReadOutput = arena.allocate(C_INT, 1);
        MemorySegment qualityOutput = arena.allocate(C_INT, numberValues);
        MemorySegment julianBaseDateOutput = arena.allocate(C_INT, 1);
        MemorySegment timeGranularitySecondsOutput = arena.allocate(C_INT, 1);
        MemorySegment unitsOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment typeOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment timezoneOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);

        status = hecdss_h.hec_dss_tsRetrieve(
                session.dssPointer(), pathnameInput,
                startDateInput, startTimeInput, endDateInput, endTimeInput,
                timeArrayOutput, valueArrayOutput, numberValues,
                numberValuesReadOutput, qualityOutput, qualityWidth,
                julianBaseDateOutput, timeGranularitySecondsOutput,
                unitsOutput, STRING_BUFFER_LENGTH,
                typeOutput, STRING_BUFFER_LENGTH,
                timezoneOutput, STRING_BUFFER_LENGTH
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to retrieve time series '%s' from '%s': %s (time window: %s to %s)"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status),
                                    start, end));
        }

        int count = numberValuesReadOutput.get(C_INT, 0);
        int granularity = timeGranularitySecondsOutput.get(C_INT, 0);
        String units = unitsOutput.getString(0);
        TimeSeriesDataType type = TimeSeriesDataType.fromDssString(typeOutput.getString(0));
        ZoneId timeZone = DssTimeZone.parse(timezoneOutput.getString(0));

        double[] values = valueArrayOutput.asSlice(0,
                (long) count * ValueLayout.JAVA_DOUBLE.byteSize())
                .toArray(ValueLayout.JAVA_DOUBLE);
        int[] timeDeltas = timeArrayOutput.asSlice(0,
                (long) count * ValueLayout.JAVA_INT.byteSize())
                .toArray(ValueLayout.JAVA_INT);

        Instant[] times = new Instant[count];
        for (int i = 0; i < count; i++) {
            long rawEpoch = InternalConstants.BASE_EPOCH_SECONDS + (long) timeDeltas[i] * granularity;
            times[i] = DssTimeZone.nativeToInstant(rawEpoch, timeZone);
            if (values[i] == InternalConstants.UNDEFINED_DOUBLE) {
                values[i] = Double.NaN;
            }
        }

        // Extract quality flags (null if all zeros / no quality data)
        int[] rawQuality = qualityOutput.asSlice(0,
                (long) count * ValueLayout.JAVA_INT.byteSize())
                .toArray(ValueLayout.JAVA_INT);
        int[] quality = null;
        if (qualityWidth > 0) {
            boolean hasAny = false;
            for (int q : rawQuality) {
                if (q != 0) { hasAny = true; break; }
            }
            if (hasAny) quality = rawQuality;
        }

        return DssTimeSeries.of(times, values, units, type, timeZone, quality);
    }

    private static Instant[] readRange(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment firstJulianOutput = arena.allocate(C_INT, 1);
        MemorySegment firstSecondsOutput = arena.allocate(C_INT, 1);
        MemorySegment lastJulianOutput = arena.allocate(C_INT, 1);
        MemorySegment lastSecondsOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_tsGetDateTimeRange(
                session.dssPointer(), pathnameInput, 1,
                firstJulianOutput, firstSecondsOutput,
                lastJulianOutput, lastSecondsOutput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to get date range for '%s' from '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }

        Instant start = julianToInstant(session,
                firstJulianOutput.get(C_INT, 0), firstSecondsOutput.get(C_INT, 0));
        Instant end = julianToInstant(session,
                lastJulianOutput.get(C_INT, 0), lastSecondsOutput.get(C_INT, 0));

        return new Instant[]{start, end};
    }

    private static Instant julianToInstant(DssSession session, int julian, int seconds) {
        Arena arena = session.arena();

        MemorySegment yearOutput = arena.allocate(C_INT, 1);
        MemorySegment monthOutput = arena.allocate(C_INT, 1);
        MemorySegment dayOutput = arena.allocate(C_INT, 1);

        hecdss_h.hec_dss_julianToYearMonthDay(julian, yearOutput, monthOutput, dayOutput);

        return OffsetDateTime.of(
                yearOutput.get(C_INT, 0),
                monthOutput.get(C_INT, 0),
                dayOutput.get(C_INT, 0),
                0, 0, 0, 0, ZoneOffset.UTC)
                .plusSeconds(seconds)
                .toInstant();
    }
}
