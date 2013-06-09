package com.sap.sailing.racecommittee.app.courseDesigner;

import com.google.android.gms.maps.model.LatLng;
import com.sap.sailing.domain.common.racelog.courseDesigner.BoatClassType;
import com.sap.sailing.domain.common.racelog.courseDesigner.CourseLayout;
import com.sap.sailing.domain.common.racelog.courseDesigner.NumberOfRounds;
import com.sap.sailing.domain.common.racelog.courseDesigner.TargetTime;

public class CourseDesignComputer {
    private LatLng startBoatPosition;
    private Double windSpeed;
    private Integer windDirection;
    private BoatClassType boatClass;
    private CourseLayout courseLayout;
    private NumberOfRounds numberOfRounds;
    private TargetTime targetTime;

    public LatLng getStartBoatPosition() {
        return startBoatPosition;
    }

    public CourseDesignComputer setStartBoatPosition(LatLng startBoatPosition) {
        this.startBoatPosition = startBoatPosition;
        return this;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public CourseDesignComputer setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public CourseDesignComputer setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
        return this;
    }

    public BoatClassType getBoatClass() {
        return boatClass;
    }

    public CourseDesignComputer setBoatClass(BoatClassType boatClass) {
        this.boatClass = boatClass;
        return this;
    }

    public CourseLayout getCourseLayout() {
        return courseLayout;
    }

    public CourseDesignComputer setCourseLayout(CourseLayout courseLayout) {
        this.courseLayout = courseLayout;
        return this;
    }

    public NumberOfRounds getNumberOfRounds() {
        return numberOfRounds;
    }

    public CourseDesignComputer setNumberOfRounds(NumberOfRounds numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
        return this;
    }

    public TargetTime getTargetTime() {
        return targetTime;
    }

    public CourseDesignComputer setTargetTime(TargetTime targetTime) {
        this.targetTime = targetTime;
        return this;
    }

    public CourseDesignComputer compute() {
        if(startBoatPosition != null && windSpeed != null && windDirection != null && boatClass != null && courseLayout != null && numberOfRounds != null && targetTime != null)
            return null;
        else
            throw new IllegalStateException("At least one mandatory parameter was not set in the computer!");
     }
}