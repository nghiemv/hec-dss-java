package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssTimeSeries;
import mil.army.usace.hec.dss.DssTimeWindow;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

final class DssTimeSeriesService {
    private static final int UNITS_BUFFER_LENGTH = 100;
    private static final int DATA_TYPE_BUFFER_LENGTH = 100;
    private static final int TIMEZONE_BUFFER_LENGTH = 100;

    private final DssSession dssSession;

    DssTimeSeriesService(DssSession dssSession) {
        this.dssSession = dssSession;
    }

    DssTimeSeries getTimeSeries(DssPathname pathname) {
        DssTimeWindow timeWindow = getTimeSeriesRange(pathname);
        return getTimeSeries(pathname, timeWindow);
    }

    DssTimeSeries getTimeSeries(DssPathname pathname, DssTimeWindow timeWindow) {
        Arena arena = dssSession.getMemorySession();

        DssTimeArg dssTimeArg = DssTimeArg.from(timeWindow.start(), timeWindow.end());
        int[] numberValuesAndQualityWidth = getTimeSeriesSizes(pathname, dssTimeArg);
        int numberValues = numberValuesAndQualityWidth[0];
        int qualityWidth = numberValuesAndQualityWidth[1];

        MemorySegment dssPointerInput = dssSession.getDssStackPointer();
        MemorySegment dssPathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(dssTimeArg.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(dssTimeArg.startTime());
        MemorySegment endDateInput = arena.allocateFrom(dssTimeArg.endDate());
        MemorySegment endTimeInput = arena.allocateFrom(dssTimeArg.endTime());

        MemorySegment timeArrayOutput = arena.allocate(C_INT, numberValues);
        MemorySegment valueArrayOutput = arena.allocate(C_DOUBLE, numberValues);
        MemorySegment numberValuesReadOutput = arena.allocate(C_INT, 1);
        MemorySegment qualityOutput = arena.allocate(C_INT, numberValues);
        MemorySegment julianBaseDateOutput = arena.allocate(C_INT, 1);
        MemorySegment timeGranularitySecondsOutput = arena.allocate(C_INT, 1);
        MemorySegment dataUnitsOutput = arena.allocate(C_CHAR, UNITS_BUFFER_LENGTH);
        MemorySegment dataTypeOutput = arena.allocate(C_CHAR, DATA_TYPE_BUFFER_LENGTH);
        MemorySegment timeZoneNameOutput = arena.allocate(C_CHAR, TIMEZONE_BUFFER_LENGTH);

        int status = hecdss_h.hec_dss_tsRetrieve(
                dssPointerInput,
                dssPathnameInput,
                startDateInput,
                startTimeInput,
                endDateInput,
                endTimeInput,
                timeArrayOutput,
                valueArrayOutput,
                numberValues,
                numberValuesReadOutput,
                qualityOutput,
                qualityWidth,
                julianBaseDateOutput,
                timeGranularitySecondsOutput,
                dataUnitsOutput,
                UNITS_BUFFER_LENGTH,
                dataTypeOutput,
                DATA_TYPE_BUFFER_LENGTH,
                timeZoneNameOutput,
                TIMEZONE_BUFFER_LENGTH
        );

        if (status == 0) {
            int numberValuesRead = numberValuesReadOutput.get(C_INT, 0);
            int timeGranularitySeconds = timeGranularitySecondsOutput.get(C_INT, 0);
            String dataUnits = dataUnitsOutput.getString(0);
            String dataType = dataTypeOutput.getString(0);

            // Zero-copy: slice the native segments to the exact number of values read
            MemorySegment timeSlice = timeArrayOutput.asSlice(0,
                    (long) numberValuesRead * ValueLayout.JAVA_INT.byteSize());
            MemorySegment valueSlice = valueArrayOutput.asSlice(0,
                    (long) numberValuesRead * ValueLayout.JAVA_DOUBLE.byteSize());

            return new NativeTimeSeries(timeSlice, valueSlice,
                    numberValuesRead, timeGranularitySeconds, dataUnits, dataType);
        } else {
            throw new DssException("Failed to retrieve time series");
        }
    }

    private int[] getTimeSeriesSizes(DssPathname dssPathname, DssTimeArg dssTimeArg) {
        Arena arena = dssSession.getMemorySession();

        MemorySegment dssPointer = dssSession.getDssStackPointer();
        MemorySegment dssPathnameInput = arena.allocateFrom(dssPathname.toString());
        MemorySegment startDateInput = arena.allocateFrom(dssTimeArg.startDate());
        MemorySegment startTimeInput = arena.allocateFrom(dssTimeArg.startTime());
        MemorySegment endDateInput = arena.allocateFrom(dssTimeArg.endDate());
        MemorySegment endTimeInput = arena.allocateFrom(dssTimeArg.endTime());
        MemorySegment numberValuesOutput = arena.allocate(C_INT, 1);
        MemorySegment qualityWidthOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_tsGetSizes(
                dssPointer,
                dssPathnameInput,
                startDateInput,
                startTimeInput,
                endDateInput,
                endTimeInput,
                numberValuesOutput,
                qualityWidthOutput
        );

        if (status == 0) {
            int numberValues = numberValuesOutput.get(C_INT, 0);
            int qualityWidth = qualityWidthOutput.get(C_INT, 0);
            return new int[] {numberValues, qualityWidth};
        } else {
            throw new DssException("Failed to retrieve time series size");
        }
    }

    private DssTimeWindow getTimeSeriesRange(DssPathname dssPathname) {
        Arena arena = dssSession.getMemorySession();

        MemorySegment dssPointer = dssSession.getDssStackPointer();
        MemorySegment dssPathnameInput = arena.allocateFrom(dssPathname.toString());
        int boolFullSet = 1;
        MemorySegment firstValidJulianOutput = arena.allocate(C_INT, 1);
        MemorySegment firstSecondsOutput = arena.allocate(C_INT, 1);
        MemorySegment lastValidJulianOutput = arena.allocate(C_INT, 1);
        MemorySegment lastSecondsOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_tsGetDateTimeRange(
                dssPointer,
                dssPathnameInput,
                boolFullSet,
                firstValidJulianOutput,
                firstSecondsOutput,
                lastValidJulianOutput,
                lastSecondsOutput
        );

        if (status == 0) {
            int firstValidJulian = firstValidJulianOutput.get(C_INT, 0);
            int firstSeconds = firstSecondsOutput.get(C_INT, 0);
            Instant startTime = convertJulianToInstant(firstValidJulian, firstSeconds);

            int lastValidJulian = lastValidJulianOutput.get(C_INT, 0);
            int lastSeconds = lastSecondsOutput.get(C_INT, 0);
            Instant endTime = convertJulianToInstant(lastValidJulian, lastSeconds);

            return new DssTimeWindow(startTime, endTime);
        } else {
            throw new DssException("Failed to retrieve time series range");
        }
    }

    private Instant convertJulianToInstant(int julian, int seconds) {
        Arena arena = dssSession.getMemorySession();

        MemorySegment yearOutput = arena.allocate(C_INT, 1);
        MemorySegment monthOutput = arena.allocate(C_INT, 1);
        MemorySegment dayOutput = arena.allocate(C_INT, 1);

        hecdss_h.hec_dss_julianToYearMonthDay(
                julian,
                yearOutput,
                monthOutput,
                dayOutput
        );

        int year = yearOutput.get(C_INT, 0);
        int month = monthOutput.get(C_INT, 0);
        int day = dayOutput.get(C_INT, 0);

        OffsetDateTime offsetDateTime = OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC)
                .plusSeconds(seconds);
        return offsetDateTime.toInstant();
    }
}
