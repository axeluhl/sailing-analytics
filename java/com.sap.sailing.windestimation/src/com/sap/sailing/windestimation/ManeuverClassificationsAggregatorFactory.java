package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.maneuverclustering.ManeuverClassificationForClusteringImpl;
import com.sap.sailing.windestimation.maneuverclustering.ManeuverClusteringBasedWindEstimationTrackImpl;
import com.sap.sailing.windestimation.maneuvergraph.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.IntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverSequenceGraph;
import com.sap.sailing.windestimation.tackoutlierremoval.MeanBasedOutlierRemovalWindEstimator;
import com.sap.sailing.windestimation.tackoutlierremoval.NeighborBasedOutlierRemovalWindEstimator;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;

public class ManeuverClassificationsAggregatorFactory {

    private final ManeuverClassifiersCache maneuverClassifiersCache;

    public ManeuverClassificationsAggregatorFactory(ManeuverClassifiersCache maneuverClassifiersCache) {
        this.maneuverClassifiersCache = maneuverClassifiersCache;
    }

    public ManeuverClassificationsAggregator hmm() {
        return new ManeuverSequenceGraph(
                new BestPathsCalculator(new IntersectedWindRangeBasedTransitionProbabilitiesCalculator()));
    }

    public ManeuverClassificationsAggregator clustering() {
        return new ManeuverClassificationsAggregator() {

            @Override
            public List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
                    RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> race) {
                BoatClass boatClass = race.getCompetitorTracks().isEmpty() ? null
                        : race.getCompetitorTracks().get(0).getBoatClass();
                ManeuverClusteringBasedWindEstimationTrackImpl windEstimator = new ManeuverClusteringBasedWindEstimationTrackImpl(
                        race, boatClass, maneuverClassifiersCache.getPolarDataService(), 30000);
                try {
                    windEstimator.initialize();
                    List<ManeuverWithEstimatedType> tackManeuvers = windEstimator.getTackClusters().stream()
                            .flatMap(cluster -> cluster.stream())
                            .map(maneuverClassification -> new ManeuverWithEstimatedType(
                                    ((ManeuverClassificationForClusteringImpl) maneuverClassification).getManeuver(),
                                    ManeuverTypeForClassification.TACK,
                                    maneuverClassification.getLikelihoodForManeuverType(ManeuverType.TACK)))
                            .collect(Collectors.toList());
                    List<ManeuverWithEstimatedType> jibeManeuvers = windEstimator.getJibeClusters().stream()
                            .flatMap(cluster -> cluster.stream())
                            .map(maneuverClassification -> new ManeuverWithEstimatedType(
                                    ((ManeuverClassificationForClusteringImpl) maneuverClassification).getManeuver(),
                                    ManeuverTypeForClassification.JIBE,
                                    maneuverClassification.getLikelihoodForManeuverType(ManeuverType.JIBE)))
                            .collect(Collectors.toList());
                    List<ManeuverWithEstimatedType> result = new ArrayList<>();
                    result.addAll(tackManeuvers);
                    result.addAll(jibeManeuvers);
                    Collections.sort(result, (one, two) -> one.getManeuver().getManeuverTimePoint()
                            .compareTo(two.getManeuver().getManeuverTimePoint()));
                    return result;
                } catch (NotEnoughDataHasBeenAddedException e) {
                    return Collections.emptyList();
                }
            }
        };
    }

    public ManeuverClassificationsAggregator meanOutlier() {
        return new MeanBasedOutlierRemovalWindEstimator(new MiddleCourseBasedTwdCalculatorImpl());
    }

    public ManeuverClassificationsAggregator neighborOutlier() {
        return new NeighborBasedOutlierRemovalWindEstimator(new MiddleCourseBasedTwdCalculatorImpl());
    }

}
