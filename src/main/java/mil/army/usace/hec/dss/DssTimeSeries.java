package mil.army.usace.hec.dss;

import java.time.Instant;

public interface DssTimeSeries {
    Instant[] times();
    double[] values();
    String dataUnits();
    String dataType();
}
