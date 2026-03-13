package mil.army.usace.hec.dss;

import java.time.Instant;
import java.util.stream.IntStream;

public interface DssTimeSeries {
    int size();
    double value(int index);
    Instant time(int index);
    String dataUnits();
    String dataType();

    /**
     * Returns a view of this time series with undefined/missing values excluded.
     * No data is copied — the returned series reads from the same backing storage.
     */
    default DssTimeSeries dropNa() {
        int[] indices = IntStream.range(0, size())
                .filter(i -> value(i) != DssConstants.UNDEFINED_DOUBLE)
                .toArray();
        if (indices.length == size()) return this;
        DssTimeSeries parent = this;
        return new DssTimeSeries() {
            @Override public int size() { return indices.length; }
            @Override public double value(int index) { return parent.value(indices[index]); }
            @Override public Instant time(int index) { return parent.time(indices[index]); }
            @Override public String dataUnits() { return parent.dataUnits(); }
            @Override public String dataType() { return parent.dataType(); }
        };
    }
}
