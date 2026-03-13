package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssTimeSeries;
import mil.army.usace.hec.dss.DssTimeWindow;
import mil.army.usace.hec.dss.internal.natives.ForeignLanguage;
import mil.army.usace.hec.dss.internal.natives.MemoryAllocator;
import mil.army.usace.hec.dss.internal.natives.MemoryParser;
import mil.army.usace.hec.dss.internal.util.PrimitiveArrayUtil;
import mil.army.usace.hec.dss.internal.util.TimeConverterUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.logging.Logger;

final class DssTimeSeriesService {
    private static final Logger logger = Logger.getLogger(DssTimeSeriesService.class.getName());
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
        Arena memorySession = dssSession.getMemorySession();
        MemoryAllocator memoryAllocator = MemoryAllocator.create(ForeignLanguage.C, memorySession);

        DssTimeArg dssTimeArg = DssTimeArg.from(timeWindow.start(), timeWindow.end());
        int[] numberValuesAndQualityWidth = getTimeSeriesSizes(pathname, dssTimeArg);
        int numberValues = numberValuesAndQualityWidth[0];
        int qualityWidth = numberValuesAndQualityWidth[1];
        int unitsBufferLength = UNITS_BUFFER_LENGTH;
        int dataTypeBufferLength = DATA_TYPE_BUFFER_LENGTH;
        int timeZoneBufferLength = TIMEZONE_BUFFER_LENGTH;

        MemorySegment dssPointerInput = dssSession.getDssStackPointer();
        MemorySegment dssPathnameInput = memoryAllocator.allocateString(pathname.toString());
        MemorySegment startDateInput = memoryAllocator.allocateString(dssTimeArg.startDate());
        MemorySegment startTimeInput = memoryAllocator.allocateString(dssTimeArg.startTime());
        MemorySegment endDateInput = memoryAllocator.allocateString(dssTimeArg.endDate());
        MemorySegment endTimeInput = memoryAllocator.allocateString(dssTimeArg.endTime());

        MemorySegment timeArrayOutput = memoryAllocator.allocateInts(numberValues);
        MemorySegment valueArrayOutput = memoryAllocator.allocateDoubles(numberValues);
        MemorySegment numberValuesReadOutput = memoryAllocator.allocateInts(numberValues);
        MemorySegment qualityOutput = memoryAllocator.allocateInts(numberValues);
        MemorySegment julianBaseDateOutput = memoryAllocator.allocateInts(numberValues);
        MemorySegment timeGranularitySecondsOutput = memoryAllocator.allocateInts(numberValues);
        MemorySegment dataUnitsOutput = memoryAllocator.allocateChars(unitsBufferLength);
        MemorySegment dataTypeOutput = memoryAllocator.allocateChars(dataTypeBufferLength);
        MemorySegment timeZoneNameOutput = memoryAllocator.allocateChars(timeZoneBufferLength);

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
                unitsBufferLength,
                dataTypeOutput,
                dataTypeBufferLength,
                timeZoneNameOutput,
                timeZoneBufferLength
        );

        if (status == 0) {
            int[] timeArray = MemoryParser.parseInts(timeArrayOutput);
            double[] valueArray = MemoryParser.parseDoubles(valueArrayOutput);
            int numberValuesRead = MemoryParser.parseInt(numberValuesReadOutput);

            timeArray = PrimitiveArrayUtil.trimArray(timeArray, numberValuesRead);
            valueArray = PrimitiveArrayUtil.trimArray(valueArray, numberValuesRead);

            int timeGranularitySeconds = MemoryParser.parseInt(timeGranularitySecondsOutput);
            String dataUnits = MemoryParser.parseString(dataUnitsOutput);
            String dataType = MemoryParser.parseString(dataTypeOutput);

            boolean isValid = validateOutputs(timeArray, valueArray, numberValuesRead);
            if (!isValid) {
                return DssTimeSeriesImpl.empty();
            }

            Instant[] instantTimes = TimeConverterUtil.convertToInstant(timeArray, timeGranularitySeconds);
            return new DssTimeSeriesImpl(instantTimes, valueArray, dataUnits, dataType);
        } else {
            throw new DssException("Failed to retrieve time series");
        }
    }

    private int[] getTimeSeriesSizes(DssPathname dssPathname, DssTimeArg dssTimeArg) {
        Arena memorySession = dssSession.getMemorySession();
        MemoryAllocator memoryAllocator = MemoryAllocator.create(ForeignLanguage.C, memorySession);

        MemorySegment dssPointer = dssSession.getDssStackPointer();
        MemorySegment dssPathnameInput = memoryAllocator.allocateString(dssPathname.toString());
        MemorySegment startDateInput = memoryAllocator.allocateString(dssTimeArg.startDate());
        MemorySegment startTimeInput = memoryAllocator.allocateString(dssTimeArg.startTime());
        MemorySegment endDateInput = memoryAllocator.allocateString(dssTimeArg.endDate());
        MemorySegment endTimeInput = memoryAllocator.allocateString(dssTimeArg.endTime());
        MemorySegment numberValuesOutput = memoryAllocator.allocateInts(1);
        MemorySegment qualityWidthOutput = memoryAllocator.allocateInts(1);

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
            int numberValues = MemoryParser.parseInt(numberValuesOutput);
            int qualityWidth = MemoryParser.parseInt(qualityWidthOutput);
            return new int[] {numberValues, qualityWidth};
        } else {
            throw new DssException("Failed to retrieve time series size");
        }
    }

    private DssTimeWindow getTimeSeriesRange(DssPathname dssPathname) {
        Arena memorySession = dssSession.getMemorySession();
        MemoryAllocator memoryAllocator = MemoryAllocator.create(ForeignLanguage.C, memorySession);

        MemorySegment dssPointer = dssSession.getDssStackPointer();
        MemorySegment dssPathnameInput = memoryAllocator.allocateString(dssPathname.toString());
        int boolFullSet = 1; // Returning the full dataset
        MemorySegment firstValidJulianOutput = memoryAllocator.allocateInts(1);
        MemorySegment firstSecondsOutput = memoryAllocator.allocateInts(1);
        MemorySegment lastValidJulianOutput = memoryAllocator.allocateInts(1);
        MemorySegment lastSecondsOutput = memoryAllocator.allocateInts(1);

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
            int firstValidJulian = MemoryParser.parseInt(firstValidJulianOutput);
            int firstSeconds = MemoryParser.parseInt(firstSecondsOutput);
            Instant startTime = convertJulianToInstant(firstValidJulian, firstSeconds);

            int lastValidJulian = MemoryParser.parseInt(lastValidJulianOutput);
            int lastSeconds = MemoryParser.parseInt(lastSecondsOutput);
            Instant endTime = convertJulianToInstant(lastValidJulian, lastSeconds);

            return new DssTimeWindow(startTime, endTime);
        } else {
            throw new DssException("Failed to retrieve time series range");
        }
    }

    private Instant convertJulianToInstant(int julian, int seconds) {
        Arena memorySession = dssSession.getMemorySession();
        MemoryAllocator memoryAllocator = MemoryAllocator.create(ForeignLanguage.C, memorySession);

        MemorySegment yearOutput = memoryAllocator.allocateInts(1);
        MemorySegment monthOutput = memoryAllocator.allocateInts(1);
        MemorySegment dayOutput = memoryAllocator.allocateInts(1);

        hecdss_h.hec_dss_julianToYearMonthDay(
                julian,
                yearOutput,
                monthOutput,
                dayOutput
        );

        int year = MemoryParser.parseInt(yearOutput);
        int month = MemoryParser.parseInt(monthOutput);
        int day = MemoryParser.parseInt(dayOutput);

        OffsetDateTime offsetDateTime = OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC)
                .plusSeconds(seconds);
        return offsetDateTime.toInstant();
    }

    /* Validation */
    private static boolean validateOutputs(int[] timeArray, double[] valueArray, int numberValuesRead) {
        if (timeArray.length != valueArray.length) {
            logger.warning("Time & Value Array Length Mismatch");
            return false;
        }

        if (timeArray.length != numberValuesRead) {
            logger.warning("Time & Value Array Length is different than number of values read");
            return false;
        }

        return true;
    }
}
