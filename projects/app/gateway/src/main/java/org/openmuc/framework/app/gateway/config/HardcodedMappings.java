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

                // IC3_F650PRO channels
                iecMap.put("iec61850_measurement1",
                                new Mapping(1001, "M_ME_NC_1", "DOUBLE", "IC3_F650PRO/LLN0.Mod.stVal"));
                iecMap.put("iec61850_measurement2",
                                new Mapping(1002, "M_ME_NC_1", "DOUBLE", "IC3_F650PRO/LLN0.Mod.ctlModel"));
                iecMap.put("iec61850_measurement3",
                                new Mapping(1003, "M_ME_NC_1", "DOUBLE", "IC3_F650PRO/LLN0.Beh.stVal"));
                iecMap.put("iec61850_measurement4",
                                new Mapping(1004, "M_ME_NC_1", "DOUBLE", "IC3_F650PRO/LLN0.Health.stVal"));
                iecMap.put("iec61850_measurement5",
                                new Mapping(1005, "M_ME_NC_1", "DOUBLE", "IC3_F650PRO/LLN0.Loc.stVal"));
                iecMap.put("iec61850_measurement6",
                                new Mapping(1006, "M_ME_NC_1", "DOUBLE", "IC3_F650PRO/LLN0.OpTmh.stVal"));

                // IC3_F650CON channels
                iecMap.put("iec61850_measurement7",
                                new Mapping(1007, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/LLN0.OpTmh.stVal"));
                iecMap.put("iec61850_measurement8",
                                new Mapping(1008, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/LLN0.Mod.stVal"));
                iecMap.put("iec61850_measurement9",
                                new Mapping(1009, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/LLN0.LocSta.stVal"));
                iecMap.put("iec61850_measurement10",
                                new Mapping(1010, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/LPHD1.PhyHealth.stVal"));

                IEC61850_MAP = Collections.unmodifiableMap(iecMap);

                // Modbus Channels
                Map<String, Mapping> modbusMap = new HashMap<>();

                modbusMap.put("modbus_register1",
                                new Mapping(3001, "M_ME_NC_1", "INT16", "Holding Register 1000"));
                modbusMap.put("modbus_register2",
                                new Mapping(3002, "M_ME_NC_1", "INT16", "Holding Register 1001"));
                modbusMap.put("modbus_register3",
                                new Mapping(3003, "M_ME_NC_1", "INT16", "Holding Register 1002"));
                modbusMap.put("modbus_register4",
                                new Mapping(3004, "M_ME_NC_1", "INT16", "Holding Register 1003"));
                modbusMap.put("modbus_register5",
                                new Mapping(3005, "M_ME_NC_1", "INT16", "Holding Register 1004"));

                modbusMap.put("modbus_register6",
                                new Mapping(3006, "M_ME_NC_1", "INT16", "Holding Register 1005"));
                modbusMap.put("modbus_register7",
                                new Mapping(3007, "M_ME_NC_1", "INT16", "Holding Register 1006"));
                modbusMap.put("modbus_register8",
                                new Mapping(3008, "M_ME_NC_1", "INT16", "Holding Register 1007"));
                modbusMap.put("modbus_register9",
                                new Mapping(3009, "M_ME_NC_1", "INT16", "Holding Register 1008"));
                modbusMap.put("modbus_register10",
                                new Mapping(3010, "M_ME_NC_1", "INT16", "Holding Register 1009"));

                modbusMap.put("modbus_register11",
                                new Mapping(3011, "M_ME_NC_1", "INT16", "Holding Register 1010"));

                MODBUS_MAP = Collections.unmodifiableMap(modbusMap);
        }

        private HardcodedMappings() {
        }
}