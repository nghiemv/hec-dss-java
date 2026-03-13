package mil.army.usace.hec.dss.internal.util;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

public class TimeConverterUtil {
    private static final LocalDateTime BASE_JULIAN_TIME = LocalDateTime.of(1900,1,1,0,0).minusDays(1);
    private static final ZoneOffset BASE_ZONE_OFFSET = ZoneOffset.UTC;

    private TimeConverterUtil() {
        // Utility class
    }

    public static Instant[] convertToInstant(int[] timeDeltaCounts, int timeGranularitySeconds) {
        ChronoUnit timeDeltaUnit = toChronoUnit(timeGranularitySeconds);
        return IntStream.of(timeDeltaCounts)
                .mapToObj(timeDeltaCount -> convertToInstant(timeDeltaCount, timeDeltaUnit))
                .toArray(Instant[]::new);
    }

    public static Instant convertToInstant(long timeDeltaCount, ChronoUnit timeDeltaUnit) {
        return BASE_JULIAN_TIME.plus(timeDeltaCount, timeDeltaUnit).toInstant(BASE_ZONE_OFFSET);
    }

    public static ZonedDateTime toZonedDateTime(Instant instant) {
        return instant.atZone(BASE_ZONE_OFFSET);
    }

    private static ChronoUnit toChronoUnit(int timeGranularitySeconds) {
        if (timeGranularitySeconds == 60) {
            return ChronoUnit.MINUTES;
        } else if (timeGranularitySeconds == 3600) {
            return ChronoUnit.HOURS;
        } else if (timeGranularitySeconds == 86400) {
            return ChronoUnit.DAYS;
        } else {
            throw new IllegalArgumentException("Unsupported time granularity seconds: " + timeGranularitySeconds);
        }
    }
}
