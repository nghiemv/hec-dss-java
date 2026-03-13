package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Gridded (raster) data read from or written to a DSS file.
 */
public record DssGrid(float[] data, DssGridInfo info, DssGridSpatialReference srs, DssGridStatistics stats) {
    public DssGrid {
        Objects.requireNonNull(data);
        Objects.requireNonNull(info);
        Objects.requireNonNull(srs);
        Objects.requireNonNull(stats);
    }
}
