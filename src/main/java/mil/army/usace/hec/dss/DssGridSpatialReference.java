package mil.army.usace.hec.dss;

/**
 * Spatial reference system metadata for a grid record.
 */
public record DssGridSpatialReference(
        String name, String definition, int definitionType
) { }
