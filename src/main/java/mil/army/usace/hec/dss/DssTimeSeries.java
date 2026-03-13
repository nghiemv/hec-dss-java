package mil.army.usace.hec.dss;

import java.time.Instant;
import java.util.stream.IntStream;

public interface DssTimeSeries {
    Instant[] times();
    double[] values();
    String dataUnits();
    String dataType();

    /**
     * Returns a new DssTimeSeries with undefined/missing values removed.
     */
    default DssTimeSeries dropNa() {
        double[] vals = values();
        int[] indices = IntStream.range(0, vals.length)
                .filter(i -> vals[i] != DssConstants.UNDEFINED_DOUBLE)
                .toArray();
        if (indices.length == vals.length) return this;
        Instant[] allTimes = times();
        Instant[] filteredTimes = new Instant[indices.length];
        double[] filteredValues = new double[indices.length];
        for (int i = 0; i < indices.length; i++) {
            filteredTimes[i] = allTimes[indices[i]];
            filteredValues[i] = vals[indices[i]];
        }
        String units = dataUnits();
        String type = dataType();
        return new DssTimeSeries() {
            @Override public Instant[] times() { return filteredTimes; }
            @Override public double[] values() { return filteredValues; }
            @Override public String dataUnits() { return units; }
            @Override public String dataType() { return type; }
        };
    }
}
