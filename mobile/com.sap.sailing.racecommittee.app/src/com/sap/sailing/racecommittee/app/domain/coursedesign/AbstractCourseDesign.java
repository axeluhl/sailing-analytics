package com.sap.sailing.racecommittee.app.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractCourseDesign implements CourseDesign{
    Position startBoatPosition;
    Double windSpeed;
    Bearing windDirection;
    PositionedMark pinEnd;
    Position referencePoint;
    Set<PositionedMark> courseDesignSpecificMarks = new HashSet<PositionedMark>();
    String courseDesignDescription = "";
    
    @Override
    public String getCourseDesignDescription() {
        return courseDesignDescription;
    }
    
    @Override
    public void setCourseDesignDescription(String courseDesignDescription) {
        this.courseDesignDescription = courseDesignDescription;
    }

    @Override
    public Set<PositionedMark> getCourseDesignSpecificMarks() {
        return courseDesignSpecificMarks;
    }

    @Override
    public void setCourseDesignSpecificMarks(Set<PositionedMark> courseDesignSpecificMarks) {
        this.courseDesignSpecificMarks = courseDesignSpecificMarks;
    }
    
    @Override
    public Position getReferencePoint() {
        return referencePoint;
    }
    
    @Override
    public void setReferencePoint(Position referencePoint) {
        this.referencePoint = referencePoint;
    }

    @Override
    public PositionedMark getPinEnd() {
        return pinEnd;
    }

    @Override
    public void setPinEnd(PositionedMark pinEnd) {
        this.pinEnd = pinEnd;
    }

    @Override
    public Position getStartBoatPosition() {
        return this.startBoatPosition;
    }

    @Override
    public void setStartBoatPosition(Position startBoatPosition) {
        this.startBoatPosition = startBoatPosition;

    }

    @Override
    public Double getWindSpeed() {
        return this.windSpeed;
    }

    @Override
    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;

    }

    @Override
    public Bearing getWindDirection() {
        return this.windDirection;
    }

    @Override
    public void setWindDirection(Bearing windDirection) {
        this.windDirection = windDirection;
    }
}
