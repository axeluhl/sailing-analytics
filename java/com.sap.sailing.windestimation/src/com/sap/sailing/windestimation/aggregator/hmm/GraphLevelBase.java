package com.sap.sailing.windestimation.aggregator.hmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevelBase {

    private final ManeuverForEstimation maneuver;
    private ManeuverWithProbabilisticTypeClassification maneuverClassification;

    private final List<GraphNode> levelNodes = new ArrayList<>();

    public GraphLevelBase(ManeuverWithProbabilisticTypeClassification maneuverClassification,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.maneuver = maneuverClassification.getManeuver();
        this.maneuverClassification = maneuverClassification;
        initNodes(transitionProbabilitiesCalculator);
    }

    private void initNodes(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        initTackNode(transitionProbabilitiesCalculator);
        initJibeNode(transitionProbabilitiesCalculator);
        initHeadUpNode(transitionProbabilitiesCalculator);
        initBearAwayNode(transitionProbabilitiesCalculator);
        normalizeNodeConfidences();
    }

    private void addManeuverNode(ManeuverTypeForClassification maneuverType, Tack tackAfter, WindCourseRange windRange,
            double confidence) {
        GraphNode maneuverNode = new GraphNode(maneuverType, tackAfter, windRange, confidence, levelNodes.size());
        levelNodes.add(maneuverNode);
    }

    private void initBearAwayNode(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        Tack tackAfter = maneuver.getCourseChangeInDegrees() < 0 ? Tack.PORT : Tack.STARBOARD;
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.BEAR_AWAY);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.BEAR_AWAY);
        addManeuverNode(ManeuverTypeForClassification.BEAR_AWAY, tackAfter, windRange, confidence);
    }

    private void initHeadUpNode(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        Tack tackAfter = maneuver.getCourseChangeInDegrees() < 0 ? Tack.STARBOARD : Tack.PORT;
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.HEAD_UP);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.HEAD_UP);
        addManeuverNode(ManeuverTypeForClassification.HEAD_UP, tackAfter, windRange, confidence);
    }

    private void initJibeNode(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        WindCourseRange windRange = transitionProbabilitiesCalculator.getWindCourseRangeForManeuverType(maneuver,
                ManeuverTypeForClassification.JIBE);
        double confidence = maneuverClassification.getManeuverTypeLikelihood(ManeuverTypeForClassification.JIBE);
        Tack tackAfter = maneuver.getCourseChangeWithinMainCurveInDegrees() < 0 ? Tack.STARBOARD : Tack.PORT;
        addManeuverNode(ManeuverTypeForClassification.JIBE, tackAfter, windRange, confidence);
    }

    private void initTackNode(GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
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
