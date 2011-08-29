package com.sap.sailing.expeditionconnector;

import java.util.Set;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.udpconnector.UDPMessage;

public interface ExpeditionMessage extends UDPMessage {
    /**
     * variable ID for the GPS-measured latitude, in decimal degrees
     */
    final int ID_GPS_LAT = 48;

    /**
     * variable ID for the GPS-measured longitude, in decimal degrees
     */
    final int ID_GPS_LNG = 49;
    
    /**
     * variable ID for the GPS-measured course over ground (CoG) in decimal degrees
     */
    final int ID_GPS_COG = 50;
    
    /**
     * variable ID for the GPS-measured speed over ground (SoG)
     */
    final int ID_GPS_SOG = 50;
    
    /**
     * variable ID for the GPS-measured time as days since 31.12.1899 UTC, meaning 1.0 is 1.1.1900 0:00:00 UTC
     */
    final int ID_GPS_TIME = 146;
    
    /**
     * variable ID for magnetic heading, meaning the keel's direction, in decimal degrees
     */
    final int ID_HEADING = 13;
    
    /**
     * variable ID for true wind angle, relative to keel, in decimal degrees
     */
    final int ID_TWA = 4;
    
    /**
     * Variable ID for what Expedition thinks is the true wind direction, in decimal degrees. Some
     * users prefer to not enter the current declination for a time / place into their Expedition
     * client. In this case, the values presented for this key are the magnetic wind direction instead.
     * They should then be corrected by adding the current declination.
     */
    final int ID_TWD = 6;
    
    /**
     * variable ID for true wind speed in decimal knots
     */
    final int ID_TWS = 5;
    
    /**
     * A message's checksum determines whether the package is to be considered valid.
     */
    boolean isValid();

    /**
     * The ID of the boat that sent this message
     */
    int getBoatID();

    /**
     * Lists all variable IDs for which this message has a value
     */
    Set<Integer> getVariableIDs();

    /**
     * Tells if <code>variableID</code> appears in {@link #getVariableIDs()}.
     */
    boolean hasValue(int variableID);

    /**
     * If {@link #hasValue(int)} is <code>true</code> for <code>variableID</code>, the variable's value is returned.
     * Otherwise, an {@link IllegalArgumentException} is thrown.
     */
    double getValue(int variableID);
    
    GPSFix getGPSFix();
    
    GPSFixMoving getGPSFixMoving();
    
    /**
     * Returns what what Expedition thinks is the true wind direction, in decimal degrees. Some users prefer to not
     * enter the current declination for a time / place into their Expedition client. In this case, the values presented
     * for this key are the magnetic wind direction instead. They should then be corrected by adding the current
     * declination.
     */
    SpeedWithBearing getTrueWind();
    
    SpeedWithBearing getSpeedOverGround();

    Bearing getTrueWindBearing();

    Bearing getCourseOverGround();

    TimePoint getTimePoint();

    TimePoint getCreatedAt();
}
