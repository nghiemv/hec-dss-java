package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssConstants;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssTimeSeries;
import mil.army.usace.hec.dss.DssTimeWindow;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class TimeSeriesReader {
    private static final int STRING_BUFFER_LENGTH = NativeBuffers.STRING_BUFFER_LENGTH;

    private TimeSeriesReader() {}

    public static DssTimeSeries read(DssSession session, DssPathname pathname) {
        DssTimeWindow range = readRange(session, pathname);
        return read(session, pathname, range);
    }

    public static DssTimeSeries read(DssSession session, DssPathname pathname,
                                     DssTimeWindow timeWindow) {
        Arena arena = session.arena();
        NativeDateFormat time = NativeDateFormat.from(timeWindow.start(), timeWindow.end());

        int[] sizes = readSizes(session, pathname, time);
        int numberValues = sizes[0];
        int qualityWidth = sizes[1];

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(time.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(time.startTime());
        MemorySegment endDateInput = arena.allocateFrom(time.endDate());
        MemorySegment endTimeInput = arena.allocateFrom(time.endTime());

        MemorySegment timeArrayOutput = arena.allocate(C_INT, numberValues);
        MemorySegment valueArrayOutput = arena.allocate(C_DOUBLE, numberValues);
        MemorySegment numberValuesReadOutput = arena.allocate(C_INT, 1);
        MemorySegment qualityOutput = arena.allocate(C_INT, numberValues);
        MemorySegment julianBaseDateOutput = arena.allocate(C_INT, 1);
        MemorySegment timeGranularitySecondsOutput = arena.allocate(C_INT, 1);
        MemorySegment unitsOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment typeOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment timezoneOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);

        int status = hecdss_h.hec_dss_tsRetrieve(
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
                    "Failed to retrieve time series '%s' from '%s': native status code %d (time window: %s to %s)"
                            .formatted(pathname, session.filePath(), status,
                                    timeWindow.start(), timeWindow.end()));
        }

        int count = numberValuesReadOutput.get(C_INT, 0);
        int granularity = timeGranularitySecondsOutput.get(C_INT, 0);
        String units = unitsOutput.getString(0);
        String type = typeOutput.getString(0);

        double[] values = valueArrayOutput.asSlice(0,
                (long) count * ValueLayout.JAVA_DOUBLE.byteSize())
                .toArray(ValueLayout.JAVA_DOUBLE);
        int[] timeDeltas = timeArrayOutput.asSlice(0,
                (long) count * ValueLayout.JAVA_INT.byteSize())
                .toArray(ValueLayout.JAVA_INT);

        long[] epochSeconds = new long[count];
        for (int i = 0; i < count; i++) {
            epochSeconds[i] = DssConstants.BASE_EPOCH_SECONDS + (long) timeDeltas[i] * granularity;
        }

        return new DssTimeSeries(values, epochSeconds, units, type);
    }

    private static int[] readSizes(DssSession session, DssPathname pathname,
                                   NativeDateFormat time) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(time.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(time.startTime());
        MemorySegment endDateInput = arena.allocateFrom(time.endDate());
        MemorySegment endTimeInput = arena.allocateFrom(time.endTime());
        MemorySegment numberValuesOutput = arena.allocate(C_INT, 1);
        MemorySegment qualityWidthOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_tsGetSizes(
                session.dssPointer(), pathnameInput,
                startDateInput, startTimeInput, endDateInput, endTimeInput,
                numberValuesOutput, qualityWidthOutput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to get time series sizes for '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        return new int[]{
                numberValuesOutput.get(C_INT, 0),
                qualityWidthOutput.get(C_INT, 0)
        };
    }

    private static DssTimeWindow readRange(DssSession session, DssPathname pathname) {
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
                    "Failed to get date range for '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        Instant start = julianToInstant(session,
                firstJulianOutput.get(C_INT, 0), firstSecondsOutput.get(C_INT, 0));
        Instant end = julianToInstant(session,
                lastJulianOutput.get(C_INT, 0), lastSecondsOutput.get(C_INT, 0));

        return new DssTimeWindow(start, end);
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
