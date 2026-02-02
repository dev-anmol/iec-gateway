package org.openmuc.framework.app.gateway.dto;


import java.util.Objects;

/**
 * Mapping Configuration - Maps source protocol to IEC 104.
 *
 * PURPOSE:
 * - Define how source data point maps to IEC 104
 * - Store IOA, ASDU type, data type
 * - Support scaling and offset for Modbus
 * @author Gateway Team
 * @version 1.0
 */
public class Mapping {

    /**
     * IEC 104 Information Object Address.
     */
    private final int ioa;

    /**
     * IEC 104 Common Address (default: 1).
     */
    private final int commonAddress;

    /**
     * IEC 104 ASDU Type (e.g., "M_ME_NC_1").
     */
    private final String asduType;

    /**
     * Data type (e.g., "FLOAT", "BOOLEAN", "INT16").
     */
    private final String dataType;

    /**
     * Scaling factor (for Modbus, default: 1.0).
     * Formula: scaled_value = (raw_value * scalingFactor) + offset
     */
    private final double scalingFactor;

    /**
     * Offset (for Modbus, default: 0.0).
     */
    private final double offset;

    /**
     * Human-readable description.
     */
    private final String description;

    // ============================================================================
    // CONSTRUCTORS
    // ============================================================================

    /**
     * Simple constructor (most common usage).
     *
     * @param ioa IEC 104 IOA
     * @param asduType IEC 104 ASDU type
     * @param dataType Data type
     */
    public Mapping(int ioa, String asduType, String dataType) {
        this(ioa, 1, asduType, dataType, 1.0, 0.0, null);
    }

    /**
     * Constructor with description.
     */
    public Mapping(int ioa, String asduType, String dataType, String description) {
        this(ioa, 1, asduType, dataType, 1.0, 0.0, description);
    }

    /**
     * Constructor with scaling (for Modbus).
     */
    public Mapping(int ioa, String asduType, String dataType, double scalingFactor, double offset) {
        this(ioa, 1, asduType, dataType, scalingFactor, offset, null);
    }

    /**
     * Full constructor.
     */
    public Mapping(int ioa, int commonAddress, String asduType, String dataType,
                   double scalingFactor, double offset, String description) {
        this.ioa = ioa;
        this.commonAddress = commonAddress;
        this.asduType = asduType;
        this.dataType = dataType;
        this.scalingFactor = scalingFactor;
        this.offset = offset;
        this.description = description;
    }

    // ============================================================================
    // GETTERS
    // ============================================================================

    public int getIoa() {
        return ioa;
    }

    public int getCommonAddress() {
        return commonAddress;
    }

    public String getAsduType() {
        return asduType;
    }

    public String getDataType() {
        return dataType;
    }

    public double getScalingFactor() {
        return scalingFactor;
    }

    public double getOffset() {
        return offset;
    }

    public String getDescription() {
        return description;
    }

    // ============================================================================
    // OBJECT METHODS
    // ============================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mapping mapping = (Mapping) o;
        return ioa == mapping.ioa && commonAddress == mapping.commonAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ioa, commonAddress);
    }

    @Override
    public String toString() {
        return String.format(
                "Mapping[ioa=%d, ca=%d, type=%s, dataType=%s, scaling=%.2f, offset=%.2f, desc='%s']",
                ioa, commonAddress, asduType, dataType, scalingFactor, offset, description
        );
    }
}
