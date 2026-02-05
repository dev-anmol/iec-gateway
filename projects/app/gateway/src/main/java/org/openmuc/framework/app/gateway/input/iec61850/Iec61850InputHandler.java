package org.openmuc.framework.app.gateway.input.iec61850;

import org.openmuc.framework.app.gateway.config.HardcodedMappings;
import org.openmuc.framework.app.gateway.core.DataHolder;
import org.openmuc.framework.app.gateway.dto.DataPoint;
import org.openmuc.framework.app.gateway.dto.Mapping;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.dataaccess.RecordListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true)
public class Iec61850InputHandler {

    private static final Logger logger = LoggerFactory.getLogger(Iec61850InputHandler.class);

    @Reference
    private DataAccessService dataAccessService;

    private DataHolder dataHolder;
    private final Map<String, Channel> channels = new HashMap<>();
    private final Map<String, RecordListener> listeners = new HashMap<>();

    @Activate
    protected void activate() {
        logger.info("Activating IEC 61850 Input Handler...");

        dataHolder = DataHolder.getInstance();
        new Thread(this::initializeChannels, "Iec61850-Init").start();
    }

    private void initializeChannels() {
        try {
            Thread.sleep(3000);

            int success = 0;
            int failed = 0;
            int skipped = 0;

            List<String> channelIds = dataAccessService.getAllIds();

            if (channelIds == null || channelIds.isEmpty()) {
                logger.warn("No channels from OpenMUC");
                return;
            }

            logger.info("Found {} channels in OpenMUC", channelIds.size());

            for (String channelId : channelIds) {
                // Filter: Only process IEC 61850 channels (prefix filter)
                if (!channelId.startsWith("iec61850_")) {
                    continue; // Skip silently - belongs to another handler
                }

                Mapping mapping = HardcodedMappings.IEC61850_MAP.get(channelId);

                if (mapping == null) {
                    logger.debug("Skipping unmapped IEC61850 channel: {}", channelId);
                    skipped++;
                    continue;
                }

                try {
                    Channel channel = dataAccessService.getChannel(channelId);

                    if (channel == null) {
                        logger.error("Channel not found: {}", channelId);
                        failed++;
                        continue;
                    }

                    RecordListener listener = new Iec61850RecordListener(channelId, mapping);
                    channel.addListener(listener);

                    channels.put(channelId, channel);
                    listeners.put(channelId, listener);

                    logger.info("IEC61850: {} - IOA {}", channelId, mapping.getIoa());
                    success++;

                } catch (Exception e) {
                    logger.error("Failed {}: {}", channelId, e.getMessage());
                    failed++;
                }
            }

            logger.info("IEC61850 Init: {} OK, {} Failed, {} Skipped", success, failed, skipped);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Init interrupted", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.info("Deactivating IEC61850 Handler...");

        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            try {
                RecordListener listener = listeners.get(entry.getKey());
                if (listener != null) {
                    entry.getValue().removeListener(listener);
                }
            } catch (Exception e) {
                logger.error("Error removing listener: {}", e.getMessage());
            }
        }

        channels.clear();
        listeners.clear();
        logger.info("IEC61850 Handler deactivated");
    }

    private class Iec61850RecordListener implements RecordListener {
        private final String channelId;
        private final Mapping mapping;

        public Iec61850RecordListener(String channelId, Mapping mapping) {
            this.channelId = channelId;
            this.mapping = mapping;
        }

        @Override
        public void newRecord(Record record) {
            try {
                if (record == null) {
                    return;
                }

                if (record.getValue() == null) {
                    logger.trace("Null value for {}", channelId);
                    return;
                }

                if (record.getFlag() != Flag.VALID) {
                    logger.info("Invalid flag {} for {}", record.getFlag(), channelId);
                    return;
                }

                DataPoint dp = new DataPoint();
                dp.setId(channelId);
                dp.setIoa(mapping.getIoa());
                dp.setCommonAddress(HardcodedMappings.IEC104_COMMON_ADDRESS);
                dp.setAsduType(mapping.getAsduType());
                dp.setSourceProtocol("IEC61850");
                dp.setTimestamp(record.getTimestamp());
                dp.setValid(true);

                Object value = extractValue(record);

                if (value == null) {
                    logger.warn("Null value extracted for {}", channelId);
                    return;
                }

                dp.setValue(value);
                dataHolder.updateDataPoint(dp);

                logger.debug("IEC61850: {} IOA {} = {}", channelId, mapping.getIoa(), value);

            } catch (Exception e) {
                logger.error("Error processing {}: {}", channelId, e.getMessage());
            }
        }

        private Object extractValue(Record record) {
            try {
                ValueType type = record.getValue().getValueType();

                switch (type) {
                    case BOOLEAN:
                        return record.getValue().asBoolean();
                    case BYTE:
                        return record.getValue().asByte();
                    case SHORT:
                        return record.getValue().asShort();
                    case INTEGER:
                        return record.getValue().asInt();
                    case LONG:
                        return record.getValue().asLong();
                    case FLOAT:
                        return record.getValue().asFloat();
                    case DOUBLE:
                        return record.getValue().asDouble();
                    case STRING:
                        return record.getValue().asString();
                    case BYTE_ARRAY:
                        return record.getValue().asByteArray();
                    default:
                        logger.warn("Unknown type {} for {}", type, channelId);
                        return null;
                }

            } catch (Exception e) {
                logger.error("Error extracting value for {}: {}", channelId, e.getMessage());
                return null;
            }
        }
    }
}
