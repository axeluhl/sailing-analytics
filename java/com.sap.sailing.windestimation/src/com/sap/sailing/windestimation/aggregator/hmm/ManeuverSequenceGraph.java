package com.sap.sailing.windestimation.aggregator.hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.aggregator.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * {@link ManeuverClassificationsAggregator} which is using a sequence-based Hidden Markov Model (HMM) to aggregate
 * maneuver classifications. Maneuvers with its features are considered as observation. The maneuver type of a maneuver
 * is treated as a hidden state. The observation probability for a maneuver type of a maneuver is given by the maneuver
 * type likelihood provided in maneuver classification. The transition probability is calculated by considering TWD
 * delta between maneuvers with assumed maneuver types. The detailed strategy for transition probability derivation is
 * given by {@link BestPathsCalculator#getTransitionProbabilitiesCalculator()}. To infer the maneuver types of each
 * maneuver, Viterbi algorithm is used, which is implemented in the provided {@link BestPathsCalculator}. For wind fix
 * confidence determination, Forward-Backward algorithm is used which is also implemented in
 * {@link BestPathsCalculator}.
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
