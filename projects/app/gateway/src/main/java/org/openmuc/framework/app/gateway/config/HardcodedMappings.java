package org.openmuc.framework.app.gateway.config;

import org.openmuc.framework.app.gateway.dto.Mapping;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HardcodedMappings {

        public static final String IEC104_BIND_IP = "0.0.0.0";
        public static final int IEC104_PORT = 2404;
        public static final int IEC104_COMMON_ADDRESS = 1;
        public static final int IEC104_MAX_CONNECTIONS = 10;

        // Channel ID -> IOA mapping
        public static final Map<String, Mapping> IEC61850_MAP;
        public static final Map<String, Mapping> MODBUS_MAP;

        static {
                // IEC 61850 Channels
                Map<String, Mapping> iecMap = new HashMap<>();

                iecMap.put("iec61850_measurement1",
                                new Mapping(1001, "M_ME_NC_1", "DOUBLE", "Mod.stVal"));
                iecMap.put("iec61850_measurement2",
                                new Mapping(1002, "M_ME_NC_1", "DOUBLE", "Mod.ctlModel"));
                iecMap.put("iec61850_measurement3",
                                new Mapping(1003, "M_ME_NC_1", "DOUBLE", "Beh.stVal"));
                iecMap.put("iec61850_measurement4",
                                new Mapping(1004, "M_ME_NC_1", "DOUBLE", "Health.stVal"));
                iecMap.put("iec61850_measurement5",
                                new Mapping(1005, "M_ME_NC_1", "DOUBLE", "NamPlt.d"));

                IEC61850_MAP = Collections.unmodifiableMap(iecMap);

                // Modbus Channels
                Map<String, Mapping> modbusMap = new HashMap<>();

                modbusMap.put("modbus_register1",
                                new Mapping(3001, "M_ME_NC_1", "INT16", "Holding Register 1000"));
                modbusMap.put("modbus_register2",
                                new Mapping(3002, "M_ME_NC_1", "INT16", "Holding Register 1004"));

                MODBUS_MAP = Collections.unmodifiableMap(modbusMap);
        }

        private HardcodedMappings() {
        }
}