package com.sap.sailing.racecommittee.app.courseDesigner;

import com.google.android.gms.maps.model.LatLng;
import com.sap.sailing.domain.coursedesign.BoatClassType;
import com.sap.sailing.domain.coursedesign.CourseDesign;
import com.sap.sailing.domain.coursedesign.CourseLayout;
import com.sap.sailing.domain.coursedesign.NumberOfRounds;
import com.sap.sailing.domain.coursedesign.TargetTime;

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

    public CourseDesign compute() {
        if(startBoatPosition != null && windSpeed != null && windDirection != null && boatClass != null && courseLayout != null && numberOfRounds != null && targetTime != null)
            return null;
        else
            throw new IllegalStateException("At least one mandatory parameter was not set in the computer!");
     }
    
    private LatLng getLatLngForGivenPointDistanceAndBearing(LatLng givenPoint, int ditanceInMeters, float bearingInDegree){
        final int earthRadiusInMeters = 6371000;
        double lat = Math.asin( Math.sin(givenPoint.latitude)*Math.cos(ditanceInMeters/earthRadiusInMeters) + 
                Math.cos(givenPoint.latitude)*Math.sin(ditanceInMeters/earthRadiusInMeters)*Math.cos(bearingInDegree) );
  double lon = givenPoint.longitude + Math.atan2(Math.sin(bearingInDegree)*Math.sin(ditanceInMeters/earthRadiusInMeters)*Math.cos(givenPoint.latitude), 
                       Math.cos(ditanceInMeters/earthRadiusInMeters)-Math.sin(givenPoint.latitude)*Math.sin(lat));
        return new LatLng(lat, lon);
    }
}