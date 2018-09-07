package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.data.CoarseGrainedManeuverType;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverEstimationResult;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverNodeGraphLevel {

    private final ManeuverForEstimation maneuver;
    private ManeuverEstimationResult maneuverEstimationResult;

    private ManeuverNodeGraphLevel previousLevel = null;
    private ManeuverNodeGraphLevel nextLevel = null;

    private final List<ManeuverNode> levelNodes = new ArrayList<>();
    private double probabilitiesSum = 1.0;

    public ManeuverNodeGraphLevel(ManeuverForEstimation maneuver, ManeuverEstimationResult maneuverEstimationResult) {
        this.maneuver = maneuver;
        this.maneuverEstimationResult = maneuverEstimationResult;
        initNodes();
    }

    private void initNodes() {
        switch (maneuver.getManeuverCategory()) {
        case _360:
            initNodesAsPenaltyCircle();
            break;
        case _180:
        case SMALL:
        case WIDE:
            initNodeJustForSpeedAnalysis();
            break;
        case MARK_PASSING:
        case REGULAR:
            initNodesAsRegular();
            break;
        }
    }

    private void addManeuverNode(ManeuverTypeForClassification maneuverType, Tack tackAfter,
            WindRangeForManeuverNode windRange, boolean windRangeToExclude, double confidence) {
        ManeuverNode maneuverNode = new ManeuverNode(maneuverType, tackAfter, windRange, windRangeToExclude,
                confidence / probabilitiesSum, levelNodes.size() - 1);
        levelNodes.add(maneuverNode);
    }

    private void initNodeJustForSpeedAnalysis() {
        WindRangeForManeuverNode windRange = new WindRangeForManeuverNode(0, 360);
        addManeuverNode(ManeuverTypeForClassification.OTHER, null, windRange, false, 1.0);
    }

    private void initNodesAsRegular() {
        probabilitiesSum = 0;
        probabilitiesSum += maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.TACK);
        probabilitiesSum += maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.JIBE);
        probabilitiesSum += maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.BEAR_AWAY);
        probabilitiesSum += maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.HEAD_UP);
        for (ManeuverTypeForClassification maneuverType : ManeuverTypeForClassification.values()) {
            switch (maneuverType) {
            case TACK:
                initTackNode(probabilitiesSum);
                break;
            case JIBE:
                initJibeNode();
                break;
            case OTHER:
                initHeadUpNode();
                initBearAwayNode();
                break;
            }
        }
    }

    private void initBearAwayNode() {
        Bearing invertedCourseBefore = maneuver.getSpeedWithBearingBefore().getBearing().reverse();
        double angleTowardStarboard = Math.abs(
                invertedCourseBefore.getDifferenceTo(maneuver.getSpeedWithBearingAfter().getBearing()).getDegrees());
        // subtract minimal upwind TWA which is 30 deg plus -10 deg for buffer
        double minUpwindTwa = 20;
        angleTowardStarboard -= 20;
        Bearing from;
        Tack tackAfter;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = invertedCourseBefore.add(new DegreeBearingImpl(-minUpwindTwa));
            tackAfter = Tack.PORT;
        } else {
            from = maneuver.getSpeedWithBearingAfter().getBearing();
            tackAfter = Tack.STARBOARD;
        }
        WindRangeForManeuverNode windRange = new WindRangeForManeuverNode(from.getDegrees(), angleTowardStarboard);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.BEAR_AWAY);
        addManeuverNode(ManeuverTypeForClassification.OTHER, tackAfter, windRange, false, confidence);
    }

    private void initHeadUpNode() {
        Bearing invertedCourseAfter = maneuver.getSpeedWithBearingAfter().getBearing().reverse();
        double angleTowardStarboard = Math.abs(
                invertedCourseAfter.getDifferenceTo(maneuver.getSpeedWithBearingBefore().getBearing()).getDegrees());
        Bearing from;
        Tack tackAfter;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = maneuver.getSpeedWithBearingBefore().getBearing();
            tackAfter = Tack.STARBOARD;
        } else {
            from = invertedCourseAfter;
            tackAfter = Tack.PORT;
        }
        WindRangeForManeuverNode windRange = new WindRangeForManeuverNode(from.getDegrees(), angleTowardStarboard);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.HEAD_UP);
        addManeuverNode(ManeuverTypeForClassification.OTHER, tackAfter, windRange, false, confidence);
    }

    private void initJibeNode() {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        Double optimalJibeDeviationDeg = maneuver.getDeviationFromOptimalJibeAngleInDegrees();
        double middleAngleRange = 0;
        if (optimalJibeDeviationDeg != null) {
            middleAngleRange = Math.max(0, optimalJibeDeviationDeg * 2);
        }
        middleAngleRange += absCourseChangeDeg * 0.4;
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        WindRangeForManeuverNode windRange = new WindRangeForManeuverNode(from.getDegrees(), middleAngleRange);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.JIBE);
        Tack tackAfter = maneuver.getCourseChangeWithinMainCurveInDegrees() < 0 ? Tack.STARBOARD : Tack.PORT;
        addManeuverNode(ManeuverTypeForClassification.JIBE, tackAfter, windRange, false, confidence);
    }

    private void initTackNode(double probabilitiesSum) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        Double optimalTackDeviationDeg = maneuver.getDeviationFromOptimalTackAngleInDegrees();
        double middleAngleRange = 0;
        if (optimalTackDeviationDeg != null) {
            middleAngleRange = Math.max(0, optimalTackDeviationDeg * 2);
        }
        middleAngleRange += absCourseChangeDeg * 0.2;
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        from = from.reverse();
        WindRangeForManeuverNode windRange = new WindRangeForManeuverNode(from.getDegrees(), middleAngleRange);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.TACK);
        Tack tackAfter = maneuver.getCourseChangeWithinMainCurveInDegrees() < 0 ? Tack.PORT : Tack.STARBOARD;
        addManeuverNode(ManeuverTypeForClassification.TACK, tackAfter, windRange, false, confidence);
    }

    private void initNodesAsPenaltyCircle() {
        Bearing courseAtLowestSpeed = maneuver.getCourseAtLowestSpeed();
        Bearing from;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = courseAtLowestSpeed.add(new DegreeBearingImpl(-30.0));
        } else {
            from = courseAtLowestSpeed.add(new DegreeBearingImpl(-110.0));
        }
        from = from.reverse();
        double angleTowardStarboard = 140;
        WindRangeForManeuverNode windRange = new WindRangeForManeuverNode(from.getDegrees(), angleTowardStarboard);
        addManeuverNode(ManeuverTypeForClassification.OTHER, null, windRange, false, 0.7);
        addManeuverNode(ManeuverTypeForClassification.OTHER, null, windRange, true, 0.3);
    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

    public List<ManeuverNode> getLevelNodes() {
        return levelNodes;
    }

    protected void setNextLevel(ManeuverNodeGraphLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    public ManeuverNodeGraphLevel getNextLevel() {
        return nextLevel;
    }

    protected void setPreviousLevel(ManeuverNodeGraphLevel previousLevel) {
        this.previousLevel = previousLevel;
    }

    public ManeuverNodeGraphLevel getPreviousLevel() {
        return previousLevel;
    }

    public void appendNextManeuverNodesLevel(ManeuverNodeGraphLevel nextManeuverNodesLevel) {
        setNextLevel(nextManeuverNodesLevel);
        nextManeuverNodesLevel.setPreviousLevel(this);
    }

}
