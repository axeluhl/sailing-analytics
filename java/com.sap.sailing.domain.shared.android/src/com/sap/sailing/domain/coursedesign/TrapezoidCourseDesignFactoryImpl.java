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

public class TrapezoidCourseDesignFactoryImpl extends AbstractCourseDesignFactory {
    
    //we use 0.67 here, because also ISAF doesn't calculate with 1/3
    private final double REACH_LEG_FACTOR = 0.67;
    private final Distance FINISH_LEG_LENGTH = new NauticalMileDistance(0.15);
    private final int LUV_BUOY1_ANGLE_TO_WIND = 0;
    private final int LUV_BUOY2_ANGLE_TO_WIND = 100;
    private final Distance LUV_BUOY1_TO_LUV_BUOY2_DISTANCE = new NauticalMileDistance(0.03);
    private final int GATE_LENGTH_TO_HULL_LENGTH_FACTOR = 10;
    private final int GATE_XS_WIND_ANGLE = 270;
    private final int GATE_XP_WIND_ANGLE = 90;

    @Override
    public CourseDesign createCourseDesign(Position startBoatPosition, Double windSpeed, Bearing windDirection,
            BoatClassType boatClass, CourseLayouts courseLayout, NumberOfRounds numberOfRounds, TargetTime targetTime) {
        this.product = new WindWardLeeWardCourseDesignImpl();
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

        // gate calculation
        result.add(new PositionedMarkImpl("4S", getPositionForGivenPointDistanceAndBearing(this.product
                .getReferencePoint(), boatClass.getHullLength().scale(GATE_LENGTH_TO_HULL_LENGTH_FACTOR / 2),
                windDirection.add(new DegreeBearingImpl(GATE_XS_WIND_ANGLE)))));
        result.add(new PositionedMarkImpl("4P", getPositionForGivenPointDistanceAndBearing(this.product
                .getReferencePoint(), boatClass.getHullLength().scale(GATE_LENGTH_TO_HULL_LENGTH_FACTOR / 2),
                windDirection.add(new DegreeBearingImpl(GATE_XP_WIND_ANGLE)))));

        // luv buoy calculation
        Map<PointOfSail, Float> speedTable = null;
        for (Entry<WindRange, Map<PointOfSail, Float>> windRangeToSpeedTable : boatClass.getBoatSpeedTable().entrySet()) {
            if (windRangeToSpeedTable.getKey().isInRange(windSpeed)) {
                speedTable = windRangeToSpeedTable.getValue();
                break;
            }
        }
        if (speedTable == null) {
            throw new IllegalArgumentException(
                    "There was no speed diagram for the given boat class and the given wind.");
        }
        
        double legLength = (targetTime.getTimeInMinutes() - FINISH_LEG_LENGTH.getNauticalMiles()
                * speedTable.get(PointOfSail.Reach))
                / (speedTable.get(PointOfSail.Downwind) * numberOfRounds.getNumberOfRounds()
                        + (speedTable.get(PointOfSail.Upwind) * numberOfRounds.getNumberOfRounds()) + REACH_LEG_FACTOR
                        * speedTable.get(PointOfSail.Reach));

        Distance legDistance = new NauticalMileDistance(legLength);
        Position luvBuoyPosition = getPositionForGivenPointDistanceAndBearing(this.product.getReferencePoint(),
                legDistance, windDirection.add(new DegreeBearingImpl(LUV_BUOY1_ANGLE_TO_WIND)));
        DecimalFormat legLengthFormat = new DecimalFormat("0.00");
        result.add(new PositionedMarkImpl("1 " + legLengthFormat.format(legDistance.getNauticalMiles()),
                luvBuoyPosition));

        return result;
    }
}
