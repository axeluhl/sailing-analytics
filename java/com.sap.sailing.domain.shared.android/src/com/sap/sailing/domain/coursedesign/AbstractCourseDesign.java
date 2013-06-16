package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;

public class AbstractCourseDesign implements CourseDesign{
    Position startBoatPosition;
    Double windSpeed;
    Bearing windDirection;
    PositionedMark pinEnd;

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
