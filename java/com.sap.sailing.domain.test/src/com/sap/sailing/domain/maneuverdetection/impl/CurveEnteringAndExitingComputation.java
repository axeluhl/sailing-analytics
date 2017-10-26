package com.sap.sailing.domain.maneuverdetection.impl;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.SpeedWithBearingStep;
import com.sap.sailing.domain.tracking.impl.SpeedWithBearingStepImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Tests the computation of maneuver boundaries implemented in {@link ManeuverDetectorImpl}.
 * 
 * @author Vladislav Chumak (D069712)
 * @see com.sap.sailing.domain.tracking.Maneuver
 *
 */
public class CurveEnteringAndExitingComputation {

    private static final double maxDeltaForDouble = 0.000000000001;

    private final TimePoint referenceTimePoint = new MillisecondsTimePoint(
            Date.from(LocalDateTime.of(2017, 10, 26, 10, 0, 0).toInstant(ZoneOffset.UTC)));

    @Test
    public void testMainCurve() {
        TimePoint maneuverTimePoint = constructTimePoint(2);
        ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(null);
        // Test that bearing steps with continuous course change into the target direction wraps the whole time range of
        // analyzed steps.
        Iterable<SpeedWithBearingStep> steps = constructStepsWithBearings(0, 1, 3, 9, 10, 12);
        CurveEnterindAndExitingDetails mainCurve = maneuverDetector
                .computeEnteringAndExitingDetailsOfManeuverMainCurve(maneuverTimePoint, steps, NauticalSide.STARBOARD);
        assertEquals(constructTimePoint(0), mainCurve.getTimePointBefore());
        assertEquals(constructTimePoint(5), mainCurve.getTimePointAfter());

        // Test that outer bearing steps with opposite direction to the target course get cut off.
        steps = constructStepsWithBearings(0, 359, 3, 9, 10, 9);
        mainCurve = maneuverDetector.computeEnteringAndExitingDetailsOfManeuverMainCurve(maneuverTimePoint, steps,
                NauticalSide.STARBOARD);
        assertEquals(constructTimePoint(1), mainCurve.getTimePointBefore());
        assertEquals(constructTimePoint(4), mainCurve.getTimePointAfter());

        // Test that outer bearing steps with major direction to the target course do not get cut off due to short
        // deviations from target course within the main curve
        steps = constructStepsWithBearings(0, 10, 5, 15, 10, 20);
        mainCurve = maneuverDetector.computeEnteringAndExitingDetailsOfManeuverMainCurve(maneuverTimePoint, steps,
                NauticalSide.STARBOARD);
        assertEquals(constructTimePoint(0), mainCurve.getTimePointBefore());
        assertEquals(constructTimePoint(5), mainCurve.getTimePointAfter());

        // Test that the maneuvers duration gets zero, if the maneuver direction does not match the target direction and
        // the code inside does not crash.
        steps = constructStepsWithBearings(0, 359, 358, 357, 356, 355);
        mainCurve = maneuverDetector.computeEnteringAndExitingDetailsOfManeuverMainCurve(maneuverTimePoint, steps,
                NauticalSide.STARBOARD);
        assertEquals(maneuverTimePoint, mainCurve.getTimePointBefore());
        assertEquals(maneuverTimePoint, mainCurve.getTimePointAfter());

        // Test that the maneuvers duration gets zero, if the boat is not turning at all and
        // the code inside does not crash.
        steps = constructStepsWithBearings(0, 0, 0, 0, 0, 0);
        mainCurve = maneuverDetector.computeEnteringAndExitingDetailsOfManeuverMainCurve(maneuverTimePoint, steps,
                NauticalSide.STARBOARD);
        assertEquals(maneuverTimePoint, mainCurve.getTimePointBefore());
        assertEquals(maneuverTimePoint, mainCurve.getTimePointAfter());

    }

    @Test
    public void testManeuverCurve() {
        ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(null);
        // Test with time forward call that speed steps with continuous speed increase wraps the whole time range of
        // analyzed steps.
        Iterable<SpeedWithBearingStep> steps = constructStepsWithSpeeds(0, 1, 3, 9, 10, 12);
        CurveBoundaryExtension extension = maneuverDetector.findSpeedMaximum(steps, false, null);
        assertEquals(constructTimePoint(5), extension.getExtensionTimePoint());
        assertEquals(5, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Test with time backward call that speed steps with continuous speed decrease wraps the whole time range of
        // analyzed steps.
        steps = constructStepsWithSpeeds(12, 10, 9, 3, 1, 0);
        extension = maneuverDetector.findSpeedMaximum(steps, true, null);
        assertEquals(constructTimePoint(0), extension.getExtensionTimePoint());
        assertEquals(5, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Test with time forward call that the last step with decreasing speed gets removed
        steps = constructStepsWithSpeeds(0, 1, 3, 9, 10, 9);
        extension = maneuverDetector.findSpeedMaximum(steps, false, null);
        assertEquals(constructTimePoint(4), extension.getExtensionTimePoint());
        assertEquals(4, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Test with time backward call that the last step with decreasing speed gets removed
        steps = constructStepsWithSpeeds(9, 10, 9, 3, 1, 0);
        extension = maneuverDetector.findSpeedMaximum(steps, true, null);
        assertEquals(constructTimePoint(1), extension.getExtensionTimePoint());
        assertEquals(4, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Test with time forward call with the limit of global maximum search such that the value is found in the
        // global maximum area
        steps = constructStepsWithSpeeds(0, 1, 3, 10, 9, 20);
        TimePoint globalMaximumSearchUntilTimePoint = constructTimePoint(2);
        extension = maneuverDetector.findSpeedMaximum(steps, false, globalMaximumSearchUntilTimePoint);
        assertEquals(constructTimePoint(3), extension.getExtensionTimePoint());
        assertEquals(3, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Test with time backward call with the limit of global maximum search such that the value is found in the
        // global maximum area
        steps = constructStepsWithSpeeds(20, 9, 10, 3, 1, 0);
        extension = maneuverDetector.findSpeedMaximum(steps, true, globalMaximumSearchUntilTimePoint);
        assertEquals(constructTimePoint(2), extension.getExtensionTimePoint());
        assertEquals(3, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Another test with time forward call with the limit of global maximum search such that the value is found in
        // the local maximum area
        steps = constructStepsWithSpeeds(0, 1, 3, 10, 20, 9, 50);
        extension = maneuverDetector.findSpeedMaximum(steps, false, globalMaximumSearchUntilTimePoint);
        assertEquals(constructTimePoint(4), extension.getExtensionTimePoint());
        assertEquals(4, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

        // Another test with time backward call with the limit of global maximum search such that the value is found in
        // the local maximum area
        steps = constructStepsWithSpeeds(50, 9, 20, 10, 3, 1, 0);
        globalMaximumSearchUntilTimePoint = constructTimePoint(4);
        extension = maneuverDetector.findSpeedMaximum(steps, true, globalMaximumSearchUntilTimePoint);
        assertEquals(constructTimePoint(2), extension.getExtensionTimePoint());
        assertEquals(4, extension.getCourseChangeInDegreesWithinExtensionArea(), maxDeltaForDouble);

    }

    private Iterable<SpeedWithBearingStep> constructStepsWithBearings(double... bearingsInDegrees) {
        List<SpeedWithBearingStep> steps = new ArrayList<>(bearingsInDegrees.length);
        SpeedWithBearingStep previousStep = null;
        for (int i = 0; i < bearingsInDegrees.length; i++) {
            SpeedWithBearingStep step = constructStep(i, i, bearingsInDegrees[i], previousStep);
            steps.add(step);
            previousStep = step;

        }
        return steps;
    }

    private Iterable<SpeedWithBearingStep> constructStepsWithSpeeds(double... speedsInKnots) {
        List<SpeedWithBearingStep> steps = new ArrayList<>(speedsInKnots.length);
        SpeedWithBearingStep previousStep = null;
        for (int i = 0; i < speedsInKnots.length; i++) {
            SpeedWithBearingStep step = constructStep(i, speedsInKnots[i], i, previousStep);
            steps.add(step);
            previousStep = step;

        }
        return steps;
    }

    private SpeedWithBearingStep constructStep(double secondsAfterRefenceTimePoint, double speedInKnots,
            double bearingInDegrees, SpeedWithBearingStep previousStep) {
        Bearing bearing = new DegreeBearingImpl(bearingInDegrees);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(speedInKnots, bearing);
        TimePoint timePoint = constructTimePoint(secondsAfterRefenceTimePoint);
        double courseChangeInDegrees = previousStep == null ? 0.0
                : previousStep.getSpeedWithBearing().getBearing().getDifferenceTo(bearing).getDegrees();
        return new SpeedWithBearingStepImpl(timePoint, speedWithBearing, courseChangeInDegrees);
    }

    private TimePoint constructTimePoint(double secondsBeforeRefenceTimePoint) {
        return referenceTimePoint.plus((long) (secondsBeforeRefenceTimePoint * 1000));
    }

}
