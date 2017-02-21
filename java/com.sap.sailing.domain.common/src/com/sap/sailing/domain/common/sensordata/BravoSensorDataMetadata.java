package com.sap.sailing.domain.common.sensordata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Metadata that defines the column structure of {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}es when
 * imported.
 * 
 * The current implementation only stores a subset of the information available during the import.
 */
public enum BravoSensorDataMetadata {

    INSTANCE;

    private static final String RIDE_HEIGHT_PORT_HULL = "RideHeightPortHull";
    private static final String RIDE_HEIGHT_STBD_HULL = "RideHeightStbdHull";
    private static final String HEEL = "Heel";
    private static final String PITCH = "ImuSensor_Pitch";

    private final int HEADER_COLUMN_OFFSET = 3;

    /**
     * 
     * Available information in file: <br/>
     * The columns field defines which information will be loaded into the system
     * 
     */
    private final List<String> fileColumns = Collections.unmodifiableList(Arrays.asList( //
            "RideHeight", RIDE_HEIGHT_PORT_HULL, RIDE_HEIGHT_STBD_HULL, HEEL, "Trim", "ImuSensor_GyroX",
            "ImuSensor_GyroY", "ImuSensor_GyroZ", PITCH, "ImuSensor_Roll", "ImuSensor_Yaw", "ImuSensor_LinearAccX",
            "ImuSensor_LinearAccY", "ImuSensor_LinearAccZ",
              "Hb_Z", "Dn_Z", "Db_Z", "LKF_ride_hgh", "LKF_ride_hgh_Position", "LKF_ride_hgh_Velocity",
              "LKF_ride_hgh_Acceleration", "LKF_ride_hgh_PositionError", "LKF_ride_hgh_VelocityError",
              "LKF_ride_hgh_AccelerationError", "Gps_Gga_PosFixTime", "Gps_Gga_Lat", "Gps_Gga_Lon", "Gps_Gga_QI",
              "Gps_Gga_HDOP", "Gps_Gga_AntHeight", "Gps_Vtg_TMG", "Gps_Vtg_SOGKnots", "Gps_Rmc_MagVar", "Gps_EastVelocity",
              "Gps_NorthVelocity", "Gps_UpVelocity", "BravoNet_Node0x0A0_Ch0", "BravoNet_Node0x0A0_Voltage",
            "BravoNet_Node0x0A0_Temperature", "BravoNet_Node0x0A0_Current"
    ));

    private final List<String> trackColumns = Collections.unmodifiableList(Arrays.asList( //
            RIDE_HEIGHT_PORT_HULL, //
            RIDE_HEIGHT_STBD_HULL, //
            HEEL, //
            PITCH //
    ));

    public final int trackColumnCount = trackColumns.size();
    public final int rideHeightPortHullColumn = getTrackColumnIndex(RIDE_HEIGHT_PORT_HULL);
    public final int rideHeightStarboardHullColumn = getTrackColumnIndex(RIDE_HEIGHT_STBD_HULL);
    public final int heelColumn = getTrackColumnIndex(HEEL);
    public final int pitchColumn = getTrackColumnIndex(PITCH);

    /**
     * The fields available in the import file
     * 
     * @return
     */
    public List<String> getFileColumns() {
        return fileColumns;
    }

    /**
     * The fields to be loaded into a track
     * 
     * @return
     */
    public List<String> getTrackColumns() {
        return trackColumns;
    }

    public boolean trackHasColumn(String columnName) {
        return trackColumns.contains(columnName);
    }

    public int getTrackColumnIndex(String columnName) {
        return trackColumns.indexOf(columnName);
    }

    public int getHeaderColumnOffset() {
        return HEADER_COLUMN_OFFSET;
    }
}
