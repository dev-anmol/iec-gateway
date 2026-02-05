package org.openmuc.framework.app.gateway.output.iec104;

import org.openmuc.framework.app.gateway.config.HardcodedMappings;
import org.openmuc.framework.app.gateway.core.DataHolder;
import org.openmuc.framework.app.gateway.dto.DataPoint;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.ConnectionEventListener;
import org.openmuc.j60870.Server;
import org.openmuc.j60870.ServerEventListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * IEC 104 Server (OSGi Component).
 * 
 * RESPONSIBILITIES:
 * - Start j60870 Server
 * - Accept SCADA client connections
 * - Manage connection lifecycle
 * - Subscribe to DataHolder for spontaneous updates
 * - Broadcast data changes to all clients
 */
@Component(immediate = true)
public class Iec104Server {

    private static final Logger logger = LoggerFactory.getLogger(Iec104Server.class);

    @Reference
    private DataAccessService dataAccessService;

    private Server server;
    private DataHolder dataHolder;
    private final List<Iec104ConnectionHandler> activeConnections = new CopyOnWriteArrayList<>();
    private Consumer<DataPoint> dataHolderListener;

    @Activate
    protected void activate() {
        logger.info("Activating IEC 104 Server...");

        try {
            // Get DataHolder instance
            dataHolder = DataHolder.getInstance();

            // Start IEC 104 Server
            startServer();

            // Register listener for spontaneous updates
            registerDataHolderListener();

            logger.info("IEC 104 Server activated successfully");
            logger.info("Listening on {}:{}",
                    HardcodedMappings.IEC104_BIND_IP,
                    HardcodedMappings.IEC104_PORT);

        } catch (Exception e) {
            logger.error("Failed to activate IEC 104 Server", e);
            throw new RuntimeException("IEC 104 Server activation failed", e);
        }
    }

    /**
     * Start j60870 Server.
     */
    private void startServer() throws IOException {
        try {
            Server.Builder builder = Server.builder();

            // Bind to configured address
            if (!"0.0.0.0".equals(HardcodedMappings.IEC104_BIND_IP)) {
                InetAddress bindAddr = InetAddress.getByName(HardcodedMappings.IEC104_BIND_IP);
                builder.setBindAddr(bindAddr);
            }

            builder.setPort(HardcodedMappings.IEC104_PORT);
            builder.setBacklog(10); // Queue for pending connections

            server = builder.build();

            // Start listening (non-blocking)
            server.start(new Iec104ServerEventListener());

            logger.info("j60870 Server started successfully");

        } catch (IOException e) {
            logger.error("Failed to start j60870 Server: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Register listener for spontaneous data updates.
     */
    private void registerDataHolderListener() {
        dataHolderListener = this::handleDataPointUpdate;
        dataHolder.addChangeListener(dataHolderListener);
        logger.info("Registered for spontaneous data updates");
    }

    /**
     * Handle data point updates from DataHolder (spontaneous transmission).
     */
    private void handleDataPointUpdate(DataPoint dataPoint) {
        if (activeConnections.isEmpty()) {
            logger.trace("No active connections, skipping update for IOA {}",
                    dataPoint.getIoa());
            return;
        }

        logger.debug("Broadcasting spontaneous update: IOA {} = {}",
                dataPoint.getIoa(), dataPoint.getValue());

        // Send to all active connections
        int successCount = 0;
        int failCount = 0;

        for (Iec104ConnectionHandler handler : activeConnections) {
            try {
                handler.sendSpontaneous(dataPoint);
                successCount++;
            } catch (Exception e) {
                logger.error("Error sending to client {}: {}",
                        handler.getClientId(), e.getMessage());
                failCount++;
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Broadcast complete: {} sent, {} failed",
                    successCount, failCount);
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.info("========================================");
        logger.info("Deactivating IEC 104 Server...");
        logger.info("========================================");

        // Remove DataHolder listener
        if (dataHolderListener != null && dataHolder != null) {
            dataHolder.removeChangeListener(dataHolderListener);
            logger.info("Removed DataHolder listener");
        }

        // Close all connections
        closeAllConnections();

        // Stop server
        if (server != null) {
            server.stop();
            logger.info("j60870 Server stopped");
        }

        logger.info("IEC 104 Server deactivated");
    }

    /**
     * Close all active connections.
     */
    private void closeAllConnections() {
        int count = activeConnections.size();
        logger.info("Closing {} active connections", count);

        for (Iec104ConnectionHandler handler : activeConnections) {
            try {
                handler.close();
            } catch (Exception e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }

        activeConnections.clear();
        logger.info("All connections closed");
    }

    // ========================================================================
    // INNER CLASS - Server Event Listener
    // ========================================================================

    /**
     * Handles server-level events from j60870.
     */
    private class Iec104ServerEventListener implements ServerEventListener {

        @Override
        public ConnectionEventListener connectionIndication(Connection connection) {
            return handleNewConnection(connection);
        }

        @Override
        public void serverStoppedListeningIndication(IOException e) {
            if (e != null) {
                logger.error("Server stopped listening: {}", e.getMessage(), e);
            } else {
                logger.info("Server stopped listening (normal shutdown)");
            }
        }

        @Override
        public void connectionAttemptFailed(IOException e) {
            logger.warn("Connection attempt failed: {}", e.getMessage());
        }
    }

    /**
     * Handle new client connection.
     * 
     * @param connection j60870 Connection object
     * @return ConnectionEventListener for this connection
     */
    private ConnectionEventListener handleNewConnection(Connection connection) {
        String clientAddress = "unknown";
        try {
            clientAddress = connection.toString();
        } catch (Exception e) {
            logger.debug("Could not get client address: {}", e.getMessage());
        }

        logger.info("New client connection from: {}", clientAddress);

        // Check max connections limit
        if (activeConnections.size() >= HardcodedMappings.IEC104_MAX_CONNECTIONS) {
            logger.warn("Max connections ({}) reached, rejecting: {}",
                    HardcodedMappings.IEC104_MAX_CONNECTIONS, clientAddress);

            try {
                connection.close();
            } catch (Exception e) {
                logger.error("Error closing rejected connection", e);
            }

            return null; // Reject connection
        }

        // Create connection handler
        try {
            Iec104ConnectionHandler handler = new Iec104ConnectionHandler(
                    connection,
                    dataHolder,
                    this::onConnectionClosed);

            activeConnections.add(handler);

            logger.info("Client connected successfully: {} (total active: {})",
                    clientAddress, activeConnections.size());

            return handler; // Return handler as ConnectionEventListener

        } catch (Exception e) {
            logger.error("Error creating connection handler for {}: {}",
                    clientAddress, e.getMessage(), e);

            try {
                connection.close();
            } catch (Exception ex) {
                logger.error("Error closing failed connection", ex);
            }

            return null;
        }
    }

    /**
     * Callback when connection closes.
     */
    private void onConnectionClosed(Iec104ConnectionHandler handler) {
        boolean removed = activeConnections.remove(handler);

        if (removed) {
            logger.info("Client disconnected: {} (remaining: {})",
                    handler.getClientId(), activeConnections.size());
        }
    }
}