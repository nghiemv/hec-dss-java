package mil.army.usace.hec.dss.internal;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

record NativeDateFormat(String startDate, String startTime, String endDate, String endTime) {
    static NativeDateFormat from(Instant start, Instant end) {
        ZonedDateTime s = start.atZone(ZoneOffset.UTC);
        ZonedDateTime e = end.atZone(ZoneOffset.UTC);
        return new NativeDateFormat(
                s.toLocalDate().toString(), s.toLocalTime().toString(),
                e.toLocalDate().toString(), e.toLocalTime().toString());
    }
}
