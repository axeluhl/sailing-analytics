package com.sap.sailing.domain.coursedesign;

import java.util.Set;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;

public class TrapezoidCourseDesignFactoryImpl extends AbstractCourseDesignFactory {

    @Override
    public CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Bearing windDirection,
            BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime) {
        this.product = new TrapezoidCourseDesignImpl();
        this.product.setStartBoatPosition(startBoatPosition);
        this.product.setWindSpeed(windSpeed);
        this.product.setWindDirection(windDirection);
        setPinEnd(boatClass, startBoatPosition, windDirection);
        return this.product;
    }

    @Override
    protected Set<PositionedMark> computeDesignSpecificMarks(Position startBoatPosition, Double windSpeed,
            Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds,
            TargetTime targetTime) {
        // TODO Auto-generated method stub
        return null;
    }

}
