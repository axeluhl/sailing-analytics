package com.sap.sailing.domain.common.sensordata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum BravoSensorDataMetadata {
    INSTANCE;

    private final String RIDE_HEIGHT = "RideHeight";
    private final List<String> columns = Collections.unmodifiableList(Arrays.asList("ImuSensor_GyroX",
            "ImuSensor_GyroY", "ImuSensor_GyroZ", "ImuSensor_Pitch", "ImuSensor_Roll", "ImuSensor_Yaw",
            "ImuSensor_LinearAccX", "ImuSensor_LinearAccY", "ImuSensor_LinearAccZ", "Hb_Z", "Dn_Z", RIDE_HEIGHT,
            "Db_Z", "Heel", "Trim", "HeightSensor_Distance", "LKF_ride_hgh", "LKF_ride_hgh_Position",
            "LKF_ride_hgh_Velocity", "LKF_ride_hgh_Acceleration", "LKF_ride_hgh_PositionError",
            "LKF_ride_hgh_VelocityError", "LKF_ride_hgh_AccelerationError"));
    
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
    
}
