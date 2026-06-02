package mil.army.usace.hec.dss.internal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

record NativeDateFormat(String startDate, String startTime, String endDate, String endTime) {
    static NativeDateFormat from(Instant start, Instant end) {
        return from(start, end, ZoneOffset.UTC);
    }

    static NativeDateFormat from(Instant start, Instant end, ZoneId zone) {
        ZonedDateTime s = start.atZone(zone);
        ZonedDateTime e = end.atZone(zone);
        return new NativeDateFormat(
                s.toLocalDate().toString(), s.toLocalTime().toString(),
                e.toLocalDate().toString(), e.toLocalTime().toString());
    }
}
