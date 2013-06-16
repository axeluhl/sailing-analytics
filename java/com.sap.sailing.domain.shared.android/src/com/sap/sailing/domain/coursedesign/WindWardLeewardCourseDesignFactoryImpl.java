package com.sap.sailing.domain.coursedesign;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;

public class WindWardLeewardCourseDesignFactoryImpl extends AbstractCourseDesignFactory {
    private final int LUV_BUOY1_ANGLE_TO_WIND  = 0;
    private final int LUV_BUOY2_ANGLE_TO_WIND  = 100;
    private final Distance LUV_BUOY1_TO_LUV_BUOY2_DISTANCE  = new NauticalMileDistance(0.03);
    @Override
    public CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Bearing windDirection,
            BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime) {
        this.product = new WindwardLeewardCourseDesignImpl();
        this.initializeCourseDesign(startBoatPosition, windSpeed, windDirection, boatClass, courseLayout,
                numberOfRounds, targetTime);
        this.finalizeCourseDesign(startBoatPosition, windSpeed, windDirection, boatClass, courseLayout, numberOfRounds,
                targetTime);
        return this.product;
    }

    @Override
    protected Set<PositionedMark> computeDesignSpecificMarks(Position startBoatPosition, Double windSpeed,
            Bearing windDirection, BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds,
            TargetTime targetTime) {
        Set<PositionedMark> result = new HashSet<PositionedMark>();
        Map<PointOfSail, Float> speedTable = null;
        for (Entry<WindRange, Map<PointOfSail, Float>> windRangeToSpeedTable : boatClass.getBoatSpeedTable().entrySet()) {
            if (windRangeToSpeedTable.getKey().isInRange(windSpeed)) {
                speedTable = windRangeToSpeedTable.getValue();
                break;
            }
        }
        if(speedTable==null){
            throw new IllegalArgumentException("There was no speed diagram for the given boat class and the given wind.");
        }
        double roundLength = targetTime.getTimeInMinutes()/numberOfRounds.getNumberOfRounds();
        double legLength = roundLength/(speedTable.get(PointOfSail.Upwind)+speedTable.get(PointOfSail.Downwind));
        Distance legDistance = new NauticalMileDistance(legLength);
        Position luvBuoyPosition = getPositionForGivenPointDistanceAndBearing(
                this.product.getReferencePoint(), legDistance,
                windDirection.add(new DegreeBearingImpl(LUV_BUOY1_ANGLE_TO_WIND)));
        DecimalFormat legLengthFormat = new DecimalFormat("0.00");
        result.add(new PositionedMarkImpl("luv buoy 1 "+legLengthFormat.format(legDistance.getNauticalMiles()) , luvBuoyPosition));
        
        result.add(new PositionedMarkImpl("luv buoy 2" , getPositionForGivenPointDistanceAndBearing(
                luvBuoyPosition, LUV_BUOY1_TO_LUV_BUOY2_DISTANCE,
                windDirection.add(new DegreeBearingImpl(LUV_BUOY2_ANGLE_TO_WIND)))));
        
        return result;
    }
}
