package com.sap.sailing.windestimation.aggregator.hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph implements ManeuverClassificationsAggregator {

    private GraphLevel firstGraphLevel = null;
    private GraphLevel lastGraphLevel = null;
    private final BestPathsCalculator bestPathsCalculator;

    public ManeuverSequenceGraph(BestPathsCalculator bestPathsCalculator) {
        this.bestPathsCalculator = bestPathsCalculator;
    }

    private void appendManeuverAsGraphLevel(ManeuverWithProbabilisticTypeClassification maneuverClassification) {
        GraphLevel newManeuverNodesLevel = new GraphLevel(maneuverClassification,
                bestPathsCalculator.getTransitionProbabilitiesCalculator());
        if (firstGraphLevel == null) {
            firstGraphLevel = newManeuverNodesLevel;
            lastGraphLevel = newManeuverNodesLevel;

        } else {
            lastGraphLevel.appendNextManeuverNodesLevel(newManeuverNodesLevel);
            lastGraphLevel = newManeuverNodesLevel;
        }
        bestPathsCalculator.computeBestPathsToNextLevel(newManeuverNodesLevel);
    }

    protected GraphLevel getFirstGraphLevel() {
        return firstGraphLevel;
    }

    protected GraphLevel getLastGraphLevel() {
        return lastGraphLevel;
    }

    public void reset() {
        firstGraphLevel = null;
        lastGraphLevel = null;
    }

    @Override
    public List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications) {
        reset();
        List<ManeuverWithProbabilisticTypeClassification> sortedManeuverClassifications = raceWithManeuverClassifications
                .getCompetitorTracks().stream().flatMap(competitorTrack -> competitorTrack.getElements().stream())
                .sorted().collect(Collectors.toList());
        for (ManeuverWithProbabilisticTypeClassification maneuverClassification : sortedManeuverClassifications) {
            appendManeuverAsGraphLevel(maneuverClassification);
        }
        List<ManeuverWithEstimatedType> maneuversWithEstimatedType = new ArrayList<>();
        List<GraphLevelInference> bestPath = bestPathsCalculator.getBestPath(lastGraphLevel);
        for (ListIterator<GraphLevelInference> iterator = bestPath.listIterator(bestPath.size()); iterator
                .hasPrevious();) {
            GraphLevelInference entry = iterator.previous();
            ManeuverWithEstimatedType maneuverWithEstimatedType = new ManeuverWithEstimatedType(
                    entry.getGraphLevel().getManeuver(), entry.getGraphNode().getManeuverType(), entry.getConfidence());
            maneuversWithEstimatedType.add(maneuverWithEstimatedType);
        }
        return maneuversWithEstimatedType;
    }

}
