package org.openmuc.framework.app.gateway.output.iec104;

import org.openmuc.framework.app.gateway.config.HardcodedMappings;
import org.openmuc.framework.app.gateway.dto.DataPoint;
import org.openmuc.j60870.*;
import org.openmuc.j60870.ie.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds complete IEC 104 ASDUs from DataPoints.
 */
public class Iec104AsduBuilder {

    private static final Logger logger = LoggerFactory.getLogger(Iec104AsduBuilder.class);

    /**
     * Build ASDU with SPONTANEOUS cause (default for data updates).
     */
    public ASdu buildMeasurementAsdu(DataPoint dp) {
        return buildMeasurementAsdu(dp, CauseOfTransmission.SPONTANEOUS);
    }

    /**
     * Build ASDU with specific Cause of Transmission.
     */
    public ASdu buildMeasurementAsdu(DataPoint dp, CauseOfTransmission cot) {
        if (dp == null) {
            logger.error("Cannot build ASDU from null DataPoint");
            return null;
        }

        try {
            String asduTypeStr = dp.getAsduType();
            if (asduTypeStr == null) {
                asduTypeStr = "M_ME_NC_1";
            }

            ASdu asdu = null;

            switch (asduTypeStr) {
                case "M_SP_NA_1": // Single point without time
                    asdu = buildSinglePoint(dp, cot, false);
                    break;

                case "M_SP_TB_1": // Single point with time
                    asdu = buildSinglePoint(dp, cot, true);
                    break;

                case "M_ME_NC_1": // Short float without time
                    asdu = buildShortFloat(dp, cot, false);
                    break;

                case "M_ME_TF_1": // Short float with time
                    asdu = buildShortFloat(dp, cot, true);
                    break;

                case "M_ME_NB_1": // Scaled value
                    asdu = buildScaledValue(dp, cot);
                    break;

                default:
                    logger.debug("Using default M_ME_NC_1 for type: {}", asduTypeStr);
                    asdu = buildShortFloat(dp, cot, false);
            }

            return asdu;

        } catch (Exception e) {
            logger.error("Error building ASDU for IOA {}: {}", dp.getIoa(), e.getMessage(), e);
            return null;
        }
    }

    private ASdu buildSinglePoint(DataPoint dp, CauseOfTransmission cot, boolean withTime) {
        boolean value = extractBoolean(dp.getValue());

        IeSinglePointWithQuality sp = new IeSinglePointWithQuality(
                value,
                false, // blocked
                false, // substituted
                false, // not topical
                !dp.isValid() // invalid
        );

        InformationElement[][] elements;
        ASduType type;

        if (withTime) {
            IeTime56 time = new IeTime56(dp.getTimestamp() > 0 ? dp.getTimestamp() : System.currentTimeMillis());
            elements = new InformationElement[][] { { sp, time } };
            type = ASduType.M_SP_TB_1;
        } else {
            elements = new InformationElement[][] { { sp } };
            type = ASduType.M_SP_NA_1;
        }

        InformationObject io = new InformationObject(dp.getIoa(), elements);

        return new ASdu(
                type,
                false,
                cot,
                false,
                false,
                0,
                dp.getCommonAddress(),
                new InformationObject[] { io });
    }

    private ASdu buildShortFloat(DataPoint dp, CauseOfTransmission cot, boolean withTime) {
        float value = extractFloat(dp.getValue());

        IeShortFloat sf = new IeShortFloat(value);
        IeQuality quality = new IeQuality(
                false, // overflow
                false, // blocked
                false, // substituted
                false, // not topical
                !dp.isValid() // invalid
        );

        InformationElement[][] elements;
        ASduType type;

        if (withTime) {
            IeTime56 time = new IeTime56(dp.getTimestamp() > 0 ? dp.getTimestamp() : System.currentTimeMillis());
            elements = new InformationElement[][] { { sf, quality, time } };
            type = ASduType.M_ME_TF_1;
        } else {
            elements = new InformationElement[][] { { sf, quality } };
            type = ASduType.M_ME_NC_1;
        }

        InformationObject io = new InformationObject(dp.getIoa(), elements);

        return new ASdu(
                type,
                false,
                cot,
                false,
                false,
                0,
                dp.getCommonAddress(),
                new InformationObject[] { io });
    }

    private ASdu buildScaledValue(DataPoint dp, CauseOfTransmission cot) {
        int value = extractInt(dp.getValue());

        // Clamp to INT16 range
        value = Math.max(-32768, Math.min(32767, value));

        IeScaledValue sv = new IeScaledValue(value);
        IeQuality quality = new IeQuality(
                false,
                false,
                false,
                false,
                !dp.isValid());

        InformationElement[][] elements = new InformationElement[][] { { sv, quality } };
        InformationObject io = new InformationObject(dp.getIoa(), elements);

        return new ASdu(
                ASduType.M_ME_NB_1,
                false,
                cot,
                false,
                false,
                0,
                dp.getCommonAddress(),
                new InformationObject[] { io });
    }

    // Helper methods
    private boolean extractBoolean(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof Number)
            return ((Number) value).intValue() != 0;
        return false;
    }

    private float extractFloat(Object value) {
        if (value == null)
            return 0.0f;
        if (value instanceof Number)
            return ((Number) value).floatValue();
        return 0.0f;
    }

    private int extractInt(Object value) {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).intValue();
        return 0;
    }
}