package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;

public interface SwissTimingMessage {
    String getRaceID();

    int getPacketID();

    TimePoint getTimestamp();

    int getGpsID();

    Position getPosition();

    Speed getSpeed();

    int getNumberOfSatellites();

    int getBatteryPercent();
    
    /**
     * @return the number of bytes that were used to encode this message
     */
    int length();
}
