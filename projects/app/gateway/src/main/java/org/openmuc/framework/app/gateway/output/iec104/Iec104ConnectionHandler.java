package org.openmuc.framework.app.gateway.output.iec104;

import org.openmuc.framework.app.gateway.config.HardcodedMappings;
import org.openmuc.framework.app.gateway.core.DataHolder;
import org.openmuc.framework.app.gateway.dto.DataPoint;
import org.openmuc.j60870.*;
import org.openmuc.j60870.ie.IeQualifierOfInterrogation;
import org.openmuc.j60870.ie.InformationObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handles an individual IEC 104 client connection.
 * Implements j60870 1.7.2 ConnectionEventListener interface.
 */
public class Iec104ConnectionHandler implements ConnectionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(Iec104ConnectionHandler.class);

    private final Connection connection;
    private final DataHolder dataHolder;
    private final Consumer<Iec104ConnectionHandler> onCloseCallback;
    private final String clientId;
    private final Iec104AsduBuilder asduBuilder;
    private volatile boolean active;

    public Iec104ConnectionHandler(
            Connection connection,
            DataHolder dataHolder,
            Consumer<Iec104ConnectionHandler> onCloseCallback) {

        this.connection = connection;
        this.dataHolder = dataHolder;
        this.onCloseCallback = onCloseCallback;
        this.clientId = "client-" + System.currentTimeMillis();
        this.asduBuilder = new Iec104AsduBuilder();
        this.active = true;

        logger.info("Connection handler created: {}", clientId);
    }

    /**
     * j60870 1.7.2 API: newASdu(Connection, ASdu)
     */
    @Override
    public void newASdu(Connection connection, ASdu asdu) {
        logger.info("Received ASDU from {}: type={}", clientId, asdu.getTypeIdentification());

        try {
            switch (asdu.getTypeIdentification()) {
                case C_IC_NA_1:
                    handleGeneralInterrogation(asdu);
                    break;

                case C_CI_NA_1:
                    handleCounterInterrogation(asdu);
                    break;

                case C_CS_NA_1:
                    handleClockSync(asdu);
                    break;

                default:
                    logger.warn("Unsupported ASDU type: {}", asdu.getTypeIdentification());
                    sendNegativeConfirmation(asdu);
            }

        } catch (Exception e) {
            logger.error("Error processing ASDU: {}", e.getMessage(), e);
        }
    }

    private void handleGeneralInterrogation(ASdu asdu) throws IOException {
        logger.info("General Interrogation from {}", clientId);

        sendActivationConfirmation(asdu);
        sendAllDataPoints();
        sendActivationTermination(asdu);
    }

    /**
     * Handle Counter Interrogation Command (C_CI_NA_1).
     * Responds with integrated totals / counter values.
     */
    private void handleCounterInterrogation(ASdu asdu) throws IOException {
        logger.info("Counter Interrogation from {}", clientId);

        sendActivationConfirmation(asdu);

        // Send all counter-type data points (M_IT_NA_1, M_IT_TB_1)
        // For now, we respond with the same data points as GI
        // In production, filter for M_IT_* types only
        sendAllDataPoints();

        sendActivationTermination(asdu);
        logger.info("Counter Interrogation complete");
    }

    private void sendAllDataPoints() {
        Map<Integer, DataPoint> allPoints = dataHolder.getAllDataPoints();
        logger.info("Sending {} data points", allPoints.size());

        int sent = 0;
        for (DataPoint dp : allPoints.values()) {
            try {
                ASdu dataAsdu = asduBuilder.buildMeasurementAsdu(dp, CauseOfTransmission.INTERROGATED_BY_STATION);
                if (dataAsdu != null) {
                    connection.send(dataAsdu);
                    sent++;
                }
            } catch (Exception e) {
                logger.error("Error sending IOA {}: {}", dp.getIoa(), e.getMessage());
            }
        }

        logger.info("GI complete: {} points sent", sent);
    }

    private void sendActivationConfirmation(ASdu requestAsdu) throws IOException {
        InformationObject[] ios = requestAsdu.getInformationObjects();
        ASdu confirmation = new ASdu(
                requestAsdu.getTypeIdentification(),
                false,
                CauseOfTransmission.ACTIVATION_CON,
                false,
                false,
                requestAsdu.getOriginatorAddress(),
                HardcodedMappings.IEC104_COMMON_ADDRESS,
                ios != null ? ios : new InformationObject[0]);
        connection.send(confirmation);
    }

    private void sendActivationTermination(ASdu requestAsdu) throws IOException {
        InformationObject[] ios = requestAsdu.getInformationObjects();
        ASdu termination = new ASdu(
                requestAsdu.getTypeIdentification(),
                false,
                CauseOfTransmission.ACTIVATION_TERMINATION,
                false,
                false,
                requestAsdu.getOriginatorAddress(),
                HardcodedMappings.IEC104_COMMON_ADDRESS,
                ios != null ? ios : new InformationObject[0]);
        connection.send(termination);
    }

    private void handleClockSync(ASdu asdu) throws IOException {
        logger.debug("Clock sync from {}", clientId);
        sendActivationConfirmation(asdu);
    }

    private void sendNegativeConfirmation(ASdu asdu) throws IOException {
        InformationObject[] ios = asdu.getInformationObjects();

        ASdu negative = new ASdu(
                asdu.getTypeIdentification(),
                false,
                CauseOfTransmission.UNKNOWN_TYPE_ID,
                false,
                false,
                asdu.getOriginatorAddress(),
                HardcodedMappings.IEC104_COMMON_ADDRESS,
                ios != null ? ios : new InformationObject[0]);
        connection.send(negative);
    }

    public void sendSpontaneous(DataPoint dataPoint) throws IOException {
        if (!active) {
            return;
        }

        ASdu asdu = asduBuilder.buildMeasurementAsdu(dataPoint, CauseOfTransmission.SPONTANEOUS);
        if (asdu != null) {
            connection.send(asdu);
            logger.trace("Sent spontaneous: IOA {} = {}", dataPoint.getIoa(), dataPoint.getValue());
        }
    }

    /**
     * j60870 1.7.2 API: connectionClosed(Connection, IOException)
     */
    @Override
    public void connectionClosed(Connection connection, IOException e) {
        if (e != null) {
            logger.warn("Connection {} closed with error: {}", clientId, e.getMessage());
        } else {
            logger.info("Connection {} closed gracefully", clientId);
        }

        active = false;

        if (onCloseCallback != null) {
            onCloseCallback.accept(this);
        }
    }

    /**
     * j60870 1.7.2 API: dataTransferStateChanged(Connection, boolean)
     */
    @Override
    public void dataTransferStateChanged(Connection connection, boolean stopped) {
        if (stopped) {
            logger.debug("Data transfer stopped for {}", clientId);
        } else {
            logger.info("Data transfer started for {}", clientId);
        }
    }

    public void close() {
        if (!active) {
            return;
        }

        logger.info("Closing connection: {}", clientId);
        active = false;

        try {
            connection.close();
        } catch (Exception e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isActive() {
        return active;
    }
}