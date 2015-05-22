package com.sap.sailing.racecommittee.app.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;

import java.util.Set;

public interface CourseDesign {
    Position getStartBoatPosition();
    void setStartBoatPosition(Position startBoatPosition);
    Double getWindSpeed();
    void setWindSpeed(Double windSpeed);
    Bearing getWindDirection();
    void setWindDirection(Bearing windDirection);
    
    PositionedMark getPinEnd();
    public void setPinEnd(PositionedMark pinEnd);
    
    Position getReferencePoint();
    public void setReferencePoint(Position referencePoint);
    void setCourseDesignSpecificMarks(Set<PositionedMark> courseDesignSpecificMarks);
    Set<PositionedMark> getCourseDesignSpecificMarks();
    
    String getCourseDesignDescription();
    void setCourseDesignDescription(String courseDesignDescription);
}
