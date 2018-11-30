package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.aggregator.advancedhmm.AdvancedManeuverGraphGenerator.AdvancedManeuverGraphComponents;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class AdvancedManeuverGraph implements ManeuverClassificationsAggregator {

    private final AdvancedBestPathsCalculator bestPathsCalculator;

    public AdvancedManeuverGraph(AdvancedBestPathsCalculator bestPathsCalculator) {
        this.bestPathsCalculator = bestPathsCalculator;
    }

    @Override
    public List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications) {
        List<ManeuverWithProbabilisticTypeClassification> sortedManeuverClassifications = raceWithManeuverClassifications
                .getCompetitorTracks().stream().flatMap(competitorTrack -> competitorTrack.getElements().stream())
                .sorted((one, two) -> one.getManeuver().getManeuverTimePoint()
                        .compareTo(two.getManeuver().getManeuverTimePoint()))
                .collect(Collectors.toList());
        if (sortedManeuverClassifications.isEmpty()) {
            return Collections.emptyList();
        }
        sortedManeuverClassifications.get(0);

        AdvancedManeuverGraphGenerator graphGenerator = new AdvancedManeuverGraphGenerator(
                bestPathsCalculator.getTransitionProbabilitiesCalculator());
        for (ManeuverWithProbabilisticTypeClassification maneuverClassification : sortedManeuverClassifications) {
            graphGenerator.addNode(maneuverClassification);
        }
        AdvancedManeuverGraphComponents graphComponents = graphGenerator.parseGraph();
        List<ManeuverWithEstimatedType> maneuversWithEstimatedType = new ArrayList<>();
        List<GraphLevelInference> bestPath = bestPathsCalculator.getBestNodes(graphComponents);
        for (GraphLevelInference inference : bestPath) {
            ManeuverWithEstimatedType maneuverWithEstimatedType = new ManeuverWithEstimatedType(
                    inference.getGraphLevel().getManeuver(), inference.getGraphNode().getManeuverType(),
                    inference.getConfidence());
            maneuversWithEstimatedType.add(maneuverWithEstimatedType);
        }
        Collections.sort(maneuversWithEstimatedType);
        return maneuversWithEstimatedType;
    }

}
