package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MstManeuverGraph implements ManeuverClassificationsAggregator {

    private final MstBestPathsCalculator bestPathsCalculator;

    public MstManeuverGraph(MstBestPathsCalculator bestPathsCalculator) {
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
        MstManeuverGraphGenerator graphGenerator = new MstManeuverGraphGenerator(
                bestPathsCalculator.getTransitionProbabilitiesCalculator());
        for (ManeuverWithProbabilisticTypeClassification maneuverClassification : sortedManeuverClassifications) {
            graphGenerator.addNode(maneuverClassification);
        }
        MstManeuverGraphComponents graphComponents = graphGenerator.parseGraph();
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
