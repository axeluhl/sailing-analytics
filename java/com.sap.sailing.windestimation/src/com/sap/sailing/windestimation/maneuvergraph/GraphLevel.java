package com.sap.sailing.windestimation.maneuvergraph;

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
public class GraphLevel {

    private static final int MIN_UPWIND_ABS_TWA = 30;
    private final ManeuverForEstimation maneuver;
    private ManeuverEstimationResult maneuverEstimationResult;

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

    private final List<GraphNode> levelNodes = new ArrayList<>();
    private double probabilitiesSum = 1.0;

    public GraphLevel(ManeuverForEstimation maneuver, ManeuverEstimationResult maneuverEstimationResult) {
        this.maneuver = maneuver;
        this.maneuverEstimationResult = maneuverEstimationResult;
        initNodes();
    }

    private void initNodes() {
        switch (maneuver.getManeuverCategory()) {
        case _360:
//            initNodesAsPenaltyCircle();
            break;
        case _180:
        case SMALL:
        case WIDE:
//            initNodeJustForSpeedAnalysis();
            break;
        case MARK_PASSING:
        case REGULAR:
            initNodesAsRegular();
            break;
        }
    }

    private void addManeuverNode(ManeuverTypeForClassification maneuverType, Tack tackAfter,
            WindCourseRange windRange, double confidence) {
        GraphNode maneuverNode = new GraphNode(maneuverType, tackAfter, windRange, confidence / probabilitiesSum,
                levelNodes.size());
        levelNodes.add(maneuverNode);
    }

//    private void initNodeJustForSpeedAnalysis() {
//        WindCourseRange windRange = new WindCourseRange(0, 360);
//        addManeuverNode(ManeuverTypeForClassification.OTHER, null, windRange, 1.0);
//    }

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
        double angleTowardStarboard = invertedCourseBefore
                .getDifferenceTo(maneuver.getSpeedWithBearingAfter().getBearing()).abs().getDegrees();
        angleTowardStarboard -= MIN_UPWIND_ABS_TWA;
        assert (angleTowardStarboard > 0);
        Bearing from;
        Tack tackAfter;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = invertedCourseBefore.add(new DegreeBearingImpl(MIN_UPWIND_ABS_TWA));
            tackAfter = Tack.PORT;
        } else {
            from = maneuver.getSpeedWithBearingAfter().getBearing();
            tackAfter = Tack.STARBOARD;
        }
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), angleTowardStarboard);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.BEAR_AWAY);
        addManeuverNode(ManeuverTypeForClassification.OTHER, tackAfter, windRange, confidence);
    }

    private void initHeadUpNode() {
        Bearing invertedCourseAfter = maneuver.getSpeedWithBearingAfter().getBearing().reverse();
        double angleTowardStarboard = invertedCourseAfter
                .getDifferenceTo(maneuver.getSpeedWithBearingBefore().getBearing()).abs().getDegrees();
        angleTowardStarboard -= MIN_UPWIND_ABS_TWA;
        assert (angleTowardStarboard > 0);
        Bearing from;
        Tack tackAfter;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            from = maneuver.getSpeedWithBearingBefore().getBearing();
            tackAfter = Tack.STARBOARD;
        } else {
            from = invertedCourseAfter.add(new DegreeBearingImpl(MIN_UPWIND_ABS_TWA));
            tackAfter = Tack.PORT;
        }
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), angleTowardStarboard);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.HEAD_UP);
        addManeuverNode(ManeuverTypeForClassification.OTHER, tackAfter, windRange, confidence);
    }

    private void initJibeNode() {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        double middleAngleRange = absCourseChangeDeg * 0.5;
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), middleAngleRange);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.JIBE);
        Tack tackAfter = maneuver.getCourseChangeWithinMainCurveInDegrees() < 0 ? Tack.STARBOARD : Tack.PORT;
        addManeuverNode(ManeuverTypeForClassification.JIBE, tackAfter, windRange, confidence);
    }

    private void initTackNode(double probabilitiesSum) {
        Bearing middleCourse = maneuver.getMiddleCourse();
        double absCourseChangeDeg = Math.abs(maneuver.getCourseChangeInDegrees());
        double middleAngleRange = absCourseChangeDeg * 0.4;
        Bearing from = middleCourse.add(new DegreeBearingImpl(-middleAngleRange / 2.0));
        from = from.reverse();
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), middleAngleRange);
        double confidence = maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.TACK);
        Tack tackAfter = maneuver.getCourseChangeWithinMainCurveInDegrees() < 0 ? Tack.PORT : Tack.STARBOARD;
        addManeuverNode(ManeuverTypeForClassification.TACK, tackAfter, windRange, confidence);
    }

//    private void initNodesAsPenaltyCircle() {
//        Bearing courseAtLowestSpeed = maneuver.getCourseAtLowestSpeed();
//        Bearing from = courseAtLowestSpeed.add(new DegreeBearingImpl(90.0));
//        double angleTowardStarboard = 180;
//        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), angleTowardStarboard);
//        addManeuverNode(ManeuverTypeForClassification.OTHER, null, windRange, 0.7);
//        addManeuverNode(ManeuverTypeForClassification.OTHER, null, windRange.invert(), 0.3);
//    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

    public List<GraphNode> getLevelNodes() {
        return levelNodes;
    }

    protected void setNextLevel(GraphLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    public GraphLevel getNextLevel() {
        return nextLevel;
    }

    protected void setPreviousLevel(GraphLevel previousLevel) {
        this.previousLevel = previousLevel;
    }

    public GraphLevel getPreviousLevel() {
        return previousLevel;
    }

    public void appendNextManeuverNodesLevel(GraphLevel nextManeuverNodesLevel) {
        setNextLevel(nextManeuverNodesLevel);
        nextManeuverNodesLevel.setPreviousLevel(this);
    }

}
