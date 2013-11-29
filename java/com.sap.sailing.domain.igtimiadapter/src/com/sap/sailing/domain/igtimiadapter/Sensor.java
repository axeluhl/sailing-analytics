package com.sap.sailing.domain.igtimiadapter;

public interface Sensor {
    /**
     * The ID called "sensor ID" by Igtimi. This identifies the microcontroller-like device that can mange several sensors
     * such as wind, GPS or heart rate. Together with the type code and the optional {@link #getDeviceId() device ID} as used
     * for heart rate sensors the individual sensor is uniquely defined.
     */
    String getTransmitterId();
    
    String getDeviceId();
}
