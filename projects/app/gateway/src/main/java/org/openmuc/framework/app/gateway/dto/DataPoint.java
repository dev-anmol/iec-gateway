package org.openmuc.framework.app.gateway.dto;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DataPoint {

    // ============================================================================
    // IDENTIFICATION FIELDS
    // ============================================================================

    /**
     * Unique identifier (channel ID from OpenMUC).
     *
     * EXAMPLES:
     * - "iec61850_mmxu1_active_power"
     * - "modbus_holding_reg_100"
     *
     * PURPOSE:
     * - Link back to source channel
     * - Debugging and logging
     * - Correlation with configuration
     */
    private String id;

    /**
     * Source protocol identifier.
     *
     * VALUES:
     * - "IEC61850"
     * - "MODBUS_TCP"
     * - "MODBUS_RTU" (future)
     *
     * PURPOSE:
     * - Identify data origin
     * - Protocol-specific handling
     * - Statistics and monitoring
     */
    private String sourceProtocol;

    /**
     * Source address in original protocol format.
     *
     * EXAMPLES:
     * - IEC 61850: "LD0/MMXU1.TotW.mag.f"
     * - Modbus: "HOLDING:100"
     *
     * PURPOSE:
     * - Reference to original data object
     * - Debugging
     * - Documentation
     */
    private String sourceAddress;

    // ============================================================================
    // IEC 104 ADDRESSING (Destination Protocol)
    // ============================================================================

    /**
     * Information Object Address (IOA) for IEC 104.
     * CRITICAL:
     * - Must be unique across entire system
     * - SCADA systems expect stable IOAs
     * - Changing IOAs requires SCADA reconfiguration
     */
    private int ioa;

    /**
     * Common Address (CA) for IEC 104.
     *
     * RANGE: 1 to 65535 (2 bytes)
     * TYPICAL: 1 (single station)
     *
     * PURPOSE:
     * - Identify station/substation
     * - Group related data points
     * - Support multi-station gateways
     *
     * STANDARD VALUES:
     * - 1: Default for single-station gateway
     * - 2-255: Additional stations
     * - 65535: Global broadcast address
     */
    private int commonAddress = 1;  // Default: single station

    /**
     * ASDU Type Identifier for IEC 104.
     *
     * COMMON TYPES:
     *
     * MEASURED VALUES (Analog):
     * - "M_ME_NA_1" (9): Normalized value (-1.0 to +1.0)
     * - "M_ME_NB_1" (11): Scaled value (INT16)
     * - "M_ME_NC_1" (13): Short floating point (FLOAT)
     * - "M_ME_ND_1" (21): Normalized without quality
     * - "M_ME_TF_1" (36): Short float WITH CP56Time2a timestamp
     *
     * SINGLE POINT (Digital):
     * - "M_SP_NA_1" (1): Single point without time
     * - "M_SP_TB_1" (30): Single point WITH CP56Time2a timestamp
     *
     * DOUBLE POINT (Digital with intermediate states):
     * - "M_DP_NA_1" (3): Double point without time
     * - "M_DP_TB_1" (31): Double point WITH timestamp
     *
     * INTEGRATED TOTALS (Counters/Energy):
     * - "M_IT_NA_1" (15): Integrated totals
     * - "M_IT_TB_1" (37): Integrated totals WITH timestamp
     *
     * PACKED SINGLE POINT:
     * - "M_PS_NA_1" (20): Packed 32 bits
     *
     * SELECTION:
     * - Use timestamped types (*_T*_1) for critical data
     * - Use simple types (*_NA_1) for non-critical or high-frequency data
     * - Timestamps increase ASDU size by 7 bytes
     */
    private String asduType;

    // ============================================================================
    // DATA VALUE AND QUALITY
    // ============================================================================

    /**
     * Actual data value.
     *
     * TYPE DEPENDS ON ASDU:
     * - M_ME_NC_1, M_ME_TF_1: Float
     * - M_ME_NB_1: Short (INT16)
     * - M_SP_NA_1, M_SP_TB_1: Boolean
     * - M_DP_NA_1, M_DP_TB_1: Integer (0=indeterminate, 1=OFF, 2=ON, 3=indeterminate)
     * - M_IT_NA_1, M_IT_TB_1: Integer or Long (counter value)
     *
     * EXAMPLES:
     * - Active Power: 1234.56 (Float)
     * - Breaker Position: true (Boolean) - true=CLOSED, false=OPEN
     * - Temperature: 23.5 (Float)
     * - Counter: 1000000 (Long)
     *
     * NULL HANDLING:
     * - Should never be null after construction
     * - Use default values: 0.0f, false, 0
     */
    private Object value;

    /**
     * Data validity flag.
     *
     * TRUE: Data is valid and reliable
     * FALSE: Data is invalid or questionable
     *
     * REASONS FOR INVALID:
     * - Communication error (device offline)
     * - Out of range value
     * - Device reports bad quality
     * - Stale data (not updated recently)
     * - Initialization (no data received yet)
     *
     * IEC 104 MAPPING:
     * - true → Quality flags: IV=0 (valid)
     * - false → Quality flags: IV=1 (invalid)
     */
    private boolean valid = true;  // Default: assume valid

    /**
     * Timestamp from source device (milliseconds since epoch).
     *
     * SOURCE:
     * - IEC 61850: Device timestamp (if available)
     * - Modbus: Gateway timestamp (Modbus has no timestamps)
     *
     * FORMAT:
     * - Unix timestamp: milliseconds since 1970-01-01 00:00:00 UTC
     * - Example: 1738238130123L = 2026-01-30 10:15:30.123
     *
     * IEC 104 CONVERSION:
     * - Converted to CP56Time2a format (7 bytes)
     * - Milliseconds, seconds, minutes, hours, day, month, year
     *
     * USAGE:
     * - Display event time to operators
     * - Sequence of events analysis
     * - Time synchronization validation
     */
    private long timestamp;

    // ============================================================================
    // METADATA (Optional, for future extensions)
    // ============================================================================

    /**
     * Additional metadata (extensible).
     *
     * POTENTIAL USES:
     * - Unit of measurement: "kW", "V", "°C"
     * - Precision: decimal places to display
     * - Range limits: min/max values
     * - Alarm thresholds
     * - Custom application data
     *
     * EXAMPLE:
     * metadata.put("unit", "kW");
     * metadata.put("precision", 2);
     * metadata.put("alarm_high", 1500.0);
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Description (human-readable).
     *
     * EXAMPLES:
     * - "Feeder 1 Active Power"
     * - "Circuit Breaker CB-101 Position"
     * - "Temperature Sensor TS-01"
     *
     * PURPOSE:
     * - Logging (more readable than IOA)
     * - Diagnostics
     * - Documentation
     */
    private String description;

    /**
     * Last update time at gateway (for staleness detection).
     *
     * USAGE:
     * - Detect stale data (no updates for X seconds)
     * - Calculate update rate
     * - Performance monitoring
     *
     * DIFFERENCE FROM timestamp:
     * - timestamp: From source device (event time)
     * - lastUpdated: Gateway processing time
     */
    private long lastUpdated;

    // ============================================================================
    // CONSTRUCTORS
    // ============================================================================

    /**
     * Default constructor.
     *
     * USAGE:
     * DataPoint dp = new DataPoint();
     * dp.setIoa(1001);
     * dp.setValue(123.45f);
     * ...
     */
    public DataPoint() {
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor with essential fields.
     *
     * USAGE:
     * DataPoint dp = new DataPoint(1001, 1, "M_ME_NC_1", 123.45f, timestamp, true);
     */
    public DataPoint(int ioa, int commonAddress, String asduType, Object value, long timestamp, boolean valid) {
        this.ioa = ioa;
        this.commonAddress = commonAddress;
        this.asduType = asduType;
        this.value = value;
        this.timestamp = timestamp;
        this.valid = valid;
        this.lastUpdated = System.currentTimeMillis();
    }

    // ============================================================================
    // GETTERS AND SETTERS
    // ============================================================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceProtocol() {
        return sourceProtocol;
    }

    public void setSourceProtocol(String sourceProtocol) {
        this.sourceProtocol = sourceProtocol;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getIoa() {
        return ioa;
    }

    public void setIoa(int ioa) {
        this.ioa = ioa;
    }

    public int getCommonAddress() {
        return commonAddress;
    }

    public void setCommonAddress(int commonAddress) {
        this.commonAddress = commonAddress;
    }

    public String getAsduType() {
        return asduType;
    }

    public void setAsduType(String asduType) {
        this.asduType = asduType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.lastUpdated = System.currentTimeMillis();  // Update timestamp
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Add metadata entry.
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    /**
     * Check if data is stale (not updated recently).
     *
     * @param maxAgeSeconds Maximum age in seconds
     * @return true if data is stale
     */
    public boolean isStale(long maxAgeSeconds) {
        long ageMs = System.currentTimeMillis() - lastUpdated;
        return ageMs > (maxAgeSeconds * 1000);
    }

    /**
     * Get age of data in seconds.
     *
     * @return Age in seconds
     */
    public long getAgeSeconds() {
        return (System.currentTimeMillis() - lastUpdated) / 1000;
    }

    /**
     * Get value as specific type (type-safe).
     */
    public Float asFloat() {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        throw new ClassCastException("Value is not a number: " + value.getClass());
    }

    public Boolean asBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ClassCastException("Value is not a boolean: " + value.getClass());
    }

    public Integer asInteger() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new ClassCastException("Value is not a number: " + value.getClass());
    }

    public Long asLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new ClassCastException("Value is not a number: " + value.getClass());
    }

    // ============================================================================
    // OBJECT METHODS
    // ============================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPoint dataPoint = (DataPoint) o;
        return ioa == dataPoint.ioa &&
                commonAddress == dataPoint.commonAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ioa, commonAddress);
    }

    @Override
    public String toString() {
        return String.format(
                "DataPoint[id=%s, ioa=%d, ca=%d, type=%s, value=%s, valid=%b, age=%ds, protocol=%s]",
                id, ioa, commonAddress, asduType, value, valid, getAgeSeconds(), sourceProtocol
        );
    }

    /**
     * Detailed string representation (for debugging).
     */
    public String toDetailedString() {
        return String.format(
                "DataPoint{\n" +
                        "  id='%s',\n" +
                        "  sourceProtocol='%s',\n" +
                        "  sourceAddress='%s',\n" +
                        "  ioa=%d,\n" +
                        "  commonAddress=%d,\n" +
                        "  asduType='%s',\n" +
                        "  value=%s (%s),\n" +
                        "  valid=%b,\n" +
                        "  timestamp=%d,\n" +
                        "  lastUpdated=%d,\n" +
                        "  age=%ds,\n" +
                        "  description='%s',\n" +
                        "  metadata=%s\n" +
                        "}",
                id, sourceProtocol, sourceAddress, ioa, commonAddress, asduType,
                value, value != null ? value.getClass().getSimpleName() : "null",
                valid, timestamp, lastUpdated, getAgeSeconds(), description, metadata
        );
    }
}