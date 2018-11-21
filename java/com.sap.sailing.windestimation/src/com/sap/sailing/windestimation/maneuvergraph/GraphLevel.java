package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverTypeForInternalClassification;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevel {

    private final ManeuverForEstimation maneuver;
    private ManeuverWithProbabilisticTypeClassification maneuverClassification;

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

    private final List<GraphNode> levelNodes = new ArrayList<>();
    private final GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public GraphLevel(ManeuverWithProbabilisticTypeClassification maneuverClassification,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.maneuver = maneuverClassification.getManeuver();
        this.maneuverClassification = maneuverClassification;
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
        initNodes();
    }

    private void initNodes() {
        for (ManeuverTypeForInternalClassification maneuverType : ManeuverTypeForInternalClassification.values()) {
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
        Tack tackAfter;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            tackAfter = Tack.PORT;
        } else {
            tackAfter = Tack.STARBOARD;
        }
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.BEAR_AWAY);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.BEAR_AWAY);
        addManeuverNode(ManeuverTypeForClassification.BEAR_AWAY, tackAfter, windRange, confidence);
    }

    private void initHeadUpNode() {
        Tack tackAfter;
        if (maneuver.getCourseChangeInDegrees() < 0) {
            tackAfter = Tack.STARBOARD;
        } else {
            tackAfter = Tack.PORT;
        }
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.HEAD_UP);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.HEAD_UP);
        addManeuverNode(ManeuverTypeForClassification.HEAD_UP, tackAfter, windRange, confidence);
    }

    private void initJibeNode() {
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.JIBE);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.JIBE);
        Tack tackAfter = maneuver.getCourseChangeWithinMainCurveInDegrees() < 0 ? Tack.STARBOARD : Tack.PORT;
        addManeuverNode(ManeuverTypeForClassification.JIBE, tackAfter, windRange, confidence);
    }

    private void initTackNode() {
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.TACK);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.TACK);
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
