package org.openmuc.framework.app.gateway.input.modbus;

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
public class ModbusTcpInputHandler {

    private static final Logger logger = LoggerFactory.getLogger(ModbusTcpInputHandler.class);

    @Reference
    private DataAccessService dataAccessService;

    private DataHolder dataHolder;
    private final Map<String, Channel> channels = new HashMap<>();
    private final Map<String, RecordListener> listeners = new HashMap<>();

    @Activate
    protected void activate() {
        logger.info("Activating Modbus TCP Input Handler...");

        dataHolder = DataHolder.getInstance();
        new Thread(this::initializeChannels, "Modbus-Init").start();
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
                // Filter: Only process Modbus channels (prefix filter)
                if (!channelId.startsWith("modbus_")) {
                    continue; // Skip silently - belongs to another handler
                }

                Mapping mapping = HardcodedMappings.MODBUS_MAP.get(channelId);

                if (mapping == null) {
                    logger.debug("Skipping unmapped Modbus channel: {}", channelId);
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

                    RecordListener listener = new ModbusRecordListener(channelId, mapping);
                    channel.addListener(listener);

                    channels.put(channelId, channel);
                    listeners.put(channelId, listener);

                    logger.info("Modbus: {} - IOA {} (scale={})",
                            channelId, mapping.getIoa(), mapping.getScalingFactor());
                    success++;

                } catch (Exception e) {
                    logger.error("Failed {}: {}", channelId, e.getMessage());
                    failed++;
                }
            }

            logger.info("Modbus Init: {} OK, {} Failed, {} Skipped", success, failed, skipped);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Init interrupted", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.info("Deactivating Modbus Handler...");

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
        logger.info("Modbus Handler deactivated");
    }

    private class ModbusRecordListener implements RecordListener {
        private final String channelId;
        private final Mapping mapping;

        public ModbusRecordListener(String channelId, Mapping mapping) {
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
                dp.setSourceProtocol("MODBUS_TCP");
                dp.setTimestamp(System.currentTimeMillis());
                dp.setValid(true);

                Object rawValue = extractValue(record);

                if (rawValue == null) {
                    logger.warn("Null value extracted for {}", channelId);
                    return;
                }

                Object finalValue = applyScaling(rawValue);
                dp.setValue(finalValue);

                dataHolder.updateDataPoint(dp);

                logger.debug("Modbus: {} IOA {} = {}", channelId, mapping.getIoa(), finalValue);

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
                logger.error("Error extracting for {}: {}", channelId, e.getMessage());
                return null;
            }
        }

        private Object applyScaling(Object rawValue) {
            if (rawValue instanceof Number) {
                double raw = ((Number) rawValue).doubleValue();
                double scaled = (raw * mapping.getScalingFactor()) + mapping.getOffset();
                return (float) scaled;
            }
            return rawValue;
        }
    }
}