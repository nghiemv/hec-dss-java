package mil.army.usace.hec.dss;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class DssConstants {
    public static final double UNDEFINED_DOUBLE = (double) -Float.MAX_VALUE;
    public static final long BASE_EPOCH_SECONDS =
            OffsetDateTime.of(1899, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC).toEpochSecond();

    private DssConstants() {}
}
