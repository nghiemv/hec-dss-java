package mil.army.usace.hec.dss.internal;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

record DssTimeArg(
        String startDate,
        String startTime,
        String endDate,
        String endTime
) {
    static DssTimeArg from(Instant startTime, Instant endTime) {
        ZonedDateTime zonedStartTime = startTime.atZone(ZoneOffset.UTC);
        ZonedDateTime zonedEndTime = endTime.atZone(ZoneOffset.UTC);

        String startDateString = zonedStartTime.toLocalDate().toString();
        String startTimeString = zonedStartTime.toLocalTime().toString();
        String endDateString = zonedEndTime.toLocalDate().toString();
        String endTimeString = zonedEndTime.toLocalTime().toString();

        return new DssTimeArg(startDateString, startTimeString, endDateString, endTimeString);
    }
}
