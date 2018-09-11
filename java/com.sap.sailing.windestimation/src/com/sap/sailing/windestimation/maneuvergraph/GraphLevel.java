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

    private static final double PENALTY_CIRCLE_PROBABILITY_BONUS = 0.3;
    private static final int MIN_UPWIND_ABS_TWA = 30;
    private final ManeuverForEstimation maneuver;
    private ManeuverEstimationResult maneuverEstimationResult;

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

    private final List<GraphNode> levelNodes = new ArrayList<>();

    public GraphLevel(ManeuverForEstimation maneuver, ManeuverEstimationResult maneuverEstimationResult) {
        this.maneuver = maneuver;
        this.maneuverEstimationResult = maneuverEstimationResult;
        initNodes();
    }

    private void initNodes() {
        maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.TACK);
        maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.JIBE);
        maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.BEAR_AWAY);
        maneuverEstimationResult.getManeuverTypeLikelihood(CoarseGrainedManeuverType.HEAD_UP);
        for (ManeuverTypeForClassification maneuverType : ManeuverTypeForClassification.values()) {
            switch (maneuverType) {
            case TACK:
                initTackNode();
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
        normalizeNodeConfidences();
    }

    private void addManeuverNode(ManeuverTypeForClassification maneuverType, Tack tackAfter, WindCourseRange windRange,
            double confidence) {
        GraphNode maneuverNode = new GraphNode(maneuverType, tackAfter, windRange, confidence, levelNodes.size());
        levelNodes.add(maneuverNode);
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

    private void initTackNode() {
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

    public void upgradeLevelNodesConsideringPenaltyCircle(ManeuverForEstimation penaltyCircle) {
        Bearing courseAtLowestSpeed = penaltyCircle.getCourseAtLowestSpeed();
        Bearing from = courseAtLowestSpeed.add(new DegreeBearingImpl(90.0));
        double angleTowardStarboard = 180;
        WindCourseRange windRange = new WindCourseRange(from.getDegrees(), angleTowardStarboard);
        for (GraphNode currentNode : levelNodes) {
            IntersectedWindRange intersectedWindRange = currentNode.getValidWindRange().intersect(windRange);
            if (intersectedWindRange.getViolationRange() == 0) {
                currentNode.setConfidence(currentNode.getConfidence() + PENALTY_CIRCLE_PROBABILITY_BONUS);
            }
        }
        normalizeNodeConfidences();
    }

    private void normalizeNodeConfidences() {
        double probabilitiesSum = 0;
        for (GraphNode currentNode : levelNodes) {
            probabilitiesSum += currentNode.getConfidence();
        }
        for (GraphNode currentNode : levelNodes) {
            currentNode.setConfidence(currentNode.getConfidence() / probabilitiesSum);
        }
    }

}
