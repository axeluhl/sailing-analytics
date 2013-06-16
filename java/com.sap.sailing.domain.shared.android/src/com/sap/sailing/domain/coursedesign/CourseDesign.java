package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;

public interface CourseDesign {
    Position getStartBoatPosition();
    void setStartBoatPosition(Position startBoatPosition);
    Double getWindSpeed();
    void setWindSpeed(Double windSpeed);
    Bearing getWindDirection();
    void setWindDirection(Bearing windDirection);
    
    PositionedMark getPinEnd();
    public void setPinEnd(PositionedMark pinEnd);
}
