package mil.army.usace.hec.dss;

/**
 * Coordinate reference system for location data.
 *
 * <p>DSS supports a small set of coordinate systems for point locations.
 * Each constant maps to an EPSG code and the corresponding native DSS
 * coordinate/datum/unit integers internally.
 */
public enum Crs {
    NONE,
    WGS84,
    NAD83
}
