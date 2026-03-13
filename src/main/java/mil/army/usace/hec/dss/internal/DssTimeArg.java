package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.internal.util.TimeConverterUtil;

import java.time.Instant;
import java.time.ZonedDateTime;

record DssTimeArg(
        String startDate,
        String startTime,
        String endDate,
        String endTime
) {
    static DssTimeArg from(Instant startTime, Instant endTime) {
        ZonedDateTime zonedStartTime = TimeConverterUtil.toZonedDateTime(startTime);
        ZonedDateTime zonedEndTime = TimeConverterUtil.toZonedDateTime(endTime);

        String startDateString = zonedStartTime.toLocalDate().toString();
        String startTimeString = zonedStartTime.toLocalTime().toString();
        String endDateString = zonedEndTime.toLocalDate().toString();
        String endTimeString = zonedEndTime.toLocalTime().toString();

        return new DssTimeArg(startDateString, startTimeString, endDateString, endTimeString);
    }
}
