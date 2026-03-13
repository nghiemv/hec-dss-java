package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Gridded (raster) data read from or written to a DSS file.
 */
public final class DssGrid {
    private final float[] data;
    private final DssGridInfo info;
    private final DssGridSpatialReference srs;
    private final DssGridStatistics stats;

    public DssGrid(float[] data, DssGridInfo info,
                   DssGridSpatialReference srs, DssGridStatistics stats) {
        this.data = Objects.requireNonNull(data);
        this.info = Objects.requireNonNull(info);
        this.srs = Objects.requireNonNull(srs);
        this.stats = Objects.requireNonNull(stats);
    }

    public float[] data() { return data; }
    public DssGridInfo info() { return info; }
    public DssGridSpatialReference srs() { return srs; }
    public DssGridStatistics stats() { return stats; }
}
