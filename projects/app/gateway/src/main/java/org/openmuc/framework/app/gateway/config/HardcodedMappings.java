package org.openmuc.framework.app.gateway.config;

import org.openmuc.framework.app.gateway.dto.Mapping;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HardcodedMappings {

        public static final String IEC104_BIND_IP = "127.0.0.1";
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

                iecMap.put("iec61850_measurement11",
                                new Mapping(1011, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.Mod.stVal"));
                iecMap.put("iec61850_measurement12",
                                new Mapping(1012, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.Beh.stVal"));
                iecMap.put("iec61850_measurement13",
                                new Mapping(1013, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.Health.stVal"));
                iecMap.put("iec61850_measurement14",
                                new Mapping(1014, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.TotW.mag.f"));

                iecMap.put("iec61850_measurement15",
                                new Mapping(1015, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.TotVAr.mag.f"));

                iecMap.put("iec61850_measurement16",
                                new Mapping(1016, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.TotVAr.instMag.f"));

                iecMap.put("iec61850_measurement17",
                                new Mapping(1017, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.TotVA.mag.f"));

                iecMap.put("iec61850_measurement18",
                                new Mapping(1018, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.TotPF.mag.f"));

                iecMap.put("iec61850_measurement19",
                                new Mapping(1019, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.Hz.mag.f"));

                iecMap.put("iec61850_measurement20",
                                new Mapping(1020, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.Hz.instMag.f"));

                iecMap.put("iec61850_measurement21",
                                new Mapping(1021, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MMXU1.TotW.mag.f"));

                iecMap.put("iec61850_measurement22",
                                new Mapping(1022, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MSQI1.Mod.stVal"));

                iecMap.put("iec61850_measurement23",
                                new Mapping(1023, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MSQI1.Beh.stVal"));

                iecMap.put("iec61850_measurement24",
                                new Mapping(1024, "M_ME_NC_1", "DOUBLE", "IC3_F650CON/MSQI1.Health.stVal"));

                iecMap.put("iec61850_measurement25",
                                new Mapping(1025, "M_ME_NA_1", "INT16", "IC3_F650CON/MMTR1.Mod.stVal"));

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

                modbusMap.put("modbus_register12",
                                new Mapping(3012, "M_ME_NC_1", "INT16", "Holding Register 1011"));

                modbusMap.put("modbus_register13",
                                new Mapping(3013, "M_ME_NC_1", "INT16", "Holding Register 1012"));

                modbusMap.put("modbus_register14",
                                new Mapping(3014, "M_ME_NC_1", "INT16", "Holding Register 1013"));

                modbusMap.put("modbus_register15",
                                new Mapping(3015, "M_ME_NC_1", "INT16", "Holding Register 1014"));

                modbusMap.put("modbus_register16",
                                new Mapping(3016, "M_ME_NC_1", "INT16", "Holding Register 1015"));

                modbusMap.put("modbus_register17",
                                new Mapping(3017, "M_ME_NC_1", "INT16", "Holding Register 1016"));

                modbusMap.put("modbus_register18",
                                new Mapping(3018, "M_ME_NC_1", "INT16", "Holding Register 1017"));

                modbusMap.put("modbus_register19",
                                new Mapping(3019, "M_ME_NC_1", "INT16", "Holding Register 1018"));

                modbusMap.put("modbus_register20",
                                new Mapping(3020, "M_ME_NC_1", "INT16", "Holding Register 1019"));

                modbusMap.put("modbus_register21",
                                new Mapping(3021, "M_ME_NC_1", "INT16", "Holding Register 1020"));

                modbusMap.put("modbus_register22",
                                new Mapping(3022, "M_ME_NC_1", "INT16", "Holding Register 1021"));

                modbusMap.put("modbus_register23",
                                new Mapping(3023, "M_ME_NC_1", "INT16", "Holding Register 1022"));

                modbusMap.put("modbus_register24",
                                new Mapping(3024, "M_ME_NC_1", "INT16", "Holding Register 1023"));

                modbusMap.put("modbus_register25",
                                new Mapping(3025, "M_ME_NC_1", "INT16", "Holding Register 1024"));

                modbusMap.put("modbus_register26",
                                new Mapping(3026, "M_ME_NC_1", "INT16", "Holding Register 1025"));

                modbusMap.put("modbus_register27",
                                new Mapping(3027, "M_ME_NC_1", "INT16", "Holding Register 1026"));

                modbusMap.put("modbus_register28",
                                new Mapping(3028, "M_ME_NC_1", "INT16", "Holding Register 1027"));

                modbusMap.put("modbus_register29",
                                new Mapping(3029, "M_ME_NC_1", "INT16", "Holding Register 1028"));

                modbusMap.put("modbus_register30",
                                new Mapping(3030, "M_ME_NC_1", "INT16", "Holding Register 1029"));

                modbusMap.put("modbus_register31",
                                new Mapping(3031, "M_ME_NC_1", "INT16", "Holding Register 1030"));

                modbusMap.put("modbus_register32",
                                new Mapping(3032, "M_ME_NC_1", "INT16", "Holding Register 1031"));

                MODBUS_MAP = Collections.unmodifiableMap(modbusMap);
        }

        private HardcodedMappings() {
        }
}