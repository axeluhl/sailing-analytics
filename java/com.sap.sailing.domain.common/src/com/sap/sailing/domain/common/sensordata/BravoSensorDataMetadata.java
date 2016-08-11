package com.sap.sailing.domain.common.sensordata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Metadata that defines the column structure of {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}es when imported.
 */
public enum BravoSensorDataMetadata {
    INSTANCE;

    private final String RIDE_HEIGHT = "RideHeight";
    private final int HEADER_COLUMN_OFFSET = 3;

    private final List<String> columns = Collections.unmodifiableList(Arrays.asList(RIDE_HEIGHT, "RideHeightPortHull",
            "RideHeightStbdHull", "Heel", "Trim", "ImuSensor_GyroX", "ImuSensor_GyroY", "ImuSensor_GyroZ",
            "ImuSensor_Pitch", "ImuSensor_Roll", "ImuSensor_Yaw", "ImuSensor_LinearAccX", "ImuSensor_LinearAccY",
            "ImuSensor_LinearAccZ", "Hb_Z", "Dn_Z", "Db_Z", "LKF_ride_hgh", "LKF_ride_hgh_Position",
            "LKF_ride_hgh_Velocity", "LKF_ride_hgh_Acceleration", "LKF_ride_hgh_PositionError",
            "LKF_ride_hgh_VelocityError", "LKF_ride_hgh_AccelerationError", "Gps_Gga_PosFixTime", "Gps_Gga_Lat",
            "Gps_Gga_Lon", "Gps_Gga_QI", "Gps_Gga_HDOP", "Gps_Gga_AntHeight", "Gps_Vtg_TMG", "Gps_Vtg_SOGKnots",
            "Gps_Rmc_MagVar", "Gps_EastVelocity", "Gps_NorthVelocity", "Gps_UpVelocity", "BravoNet_Node0x0A0_Ch0",
            "BravoNet_Node0x0A0_Voltage", "BravoNet_Node0x0A0_Temperature", "BravoNet_Node0x0A0_Current"));
    
    public final int rideHeightColumn = getColumnIndex(RIDE_HEIGHT);

    public List<String> getColumns() {
        return columns;
    }

    public boolean hasColumn(String columnName) {
        return columns.contains(columnName);
    }

    public int getColumnIndex(String columnName) {
        return columns.indexOf(columnName);
    }
    
    public int getHeaderColumnOffset() {
        return HEADER_COLUMN_OFFSET;
    }
}
