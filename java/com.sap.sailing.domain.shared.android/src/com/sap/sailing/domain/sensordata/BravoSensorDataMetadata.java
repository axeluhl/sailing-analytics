package com.sap.sailing.domain.sensordata;




public class BravoSensorDataMetadata implements SensorDataMetadata {

    private String[] columns = { "ImuSensor_GyroX", "ImuSensor_GyroY", "ImuSensor_GyroZ", "ImuSensor_Pitch",
            "ImuSensor_Roll", "ImuSensor_Yaw", "ImuSensor_LinearAccX", "ImuSensor_LinearAccY", "ImuSensor_LinearAccZ",
            "Hb_Z", "Dn_Z", "RideHeight", "Db_Z", "Heel", "Trim", "HeightSensor_Distance", "LKF_ride_hgh",
            "LKF_ride_hgh_Position", "LKF_ride_hgh_Velocity", "LKF_ride_hgh_Acceleration",
            "LKF_ride_hgh_PositionError", "LKF_ride_hgh_VelocityError", "LKF_ride_hgh_AccelerationError" };

    @Override
    public String[] getColumns() {
        return columns;
    }
}
