package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.aggregator.clustering.ManeuverClassificationForClusteringImpl;
import com.sap.sailing.windestimation.aggregator.clustering.ManeuverClusteringBasedWindEstimationTrackImpl;
import com.sap.sailing.windestimation.aggregator.hmm.BestPathsCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.ManeuverSequenceGraph;
import com.sap.sailing.windestimation.aggregator.hmm.SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.TwdTransitionClassifierBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.DistanceAndDurationAwareWindTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstBestPathsCalculatorImpl;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraph;
import com.sap.sailing.windestimation.aggregator.outlierremoval.MeanBasedOutlierRemovalWindEstimator;
import com.sap.sailing.windestimation.aggregator.outlierremoval.NeighborBasedOutlierRemovalWindEstimator;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.model.classifier.twdtransition.TwdTransitionClassifiersCache;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;

public class ManeuverClassificationsAggregatorFactory {

    private final PolarDataService polarDataService;
    private final ModelStore modelStore;
    private final long modelCacheKeepAliveMillis;
    private final boolean preloadAllModels;

    public ManeuverClassificationsAggregatorFactory(PolarDataService polarDataService, ModelStore modelStore,
            boolean preloadAllModels, long modelCacheKeepAliveMillis) {
        this.polarDataService = polarDataService;
        this.modelStore = modelStore;
        this.preloadAllModels = preloadAllModels;
        this.modelCacheKeepAliveMillis = modelCacheKeepAliveMillis;
    }

    public ManeuverClassificationsAggregator hmm(
            HmmTransitionProbabilitiesCalculator transitionProbabilitiesCalculatorType,
            boolean propagateIntersectedWindRangeOfHeadupAndBearAway) {
        GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator = null;
        switch (transitionProbabilitiesCalculatorType) {
        case SIMPLE:
            transitionProbabilitiesCalculator = new SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator(
                    propagateIntersectedWindRangeOfHeadupAndBearAway);
            break;
        case INTERSECTED:
            transitionProbabilitiesCalculator = new IntersectedWindRangeBasedTransitionProbabilitiesCalculator(
                    propagateIntersectedWindRangeOfHeadupAndBearAway);
            break;
        case CLASSIFIER:
            transitionProbabilitiesCalculator = new TwdTransitionClassifierBasedTransitionProbabilitiesCalculator(
                    new TwdTransitionClassifiersCache(modelStore, preloadAllModels, modelCacheKeepAliveMillis),
                    propagateIntersectedWindRangeOfHeadupAndBearAway);
            break;
        case GAUSSIAN_REGRESSOR:
            transitionProbabilitiesCalculator = new DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
                    new GaussianBasedTwdTransitionDistributionCache(modelStore, preloadAllModels,
                            modelCacheKeepAliveMillis),
                    propagateIntersectedWindRangeOfHeadupAndBearAway);
        }
        return new ManeuverSequenceGraph(new BestPathsCalculator(transitionProbabilitiesCalculator));
    }

    public ManeuverClassificationsAggregator clustering() {
        return new ManeuverClassificationsAggregator() {

            @Override
            public List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
                    RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> race) {
                BoatClass boatClass = race.getCompetitorTracks().isEmpty() ? null
                        : race.getCompetitorTracks().get(0).getBoatClass();
                ManeuverClusteringBasedWindEstimationTrackImpl windEstimator = new ManeuverClusteringBasedWindEstimationTrackImpl(
                        race, boatClass, polarDataService, 30000);
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

    public ManeuverClassificationsAggregator mstHmm(boolean propagateIntersectedWindRangeOfHeadupAndBearAway) {
        return new MstManeuverGraph(
                new MstBestPathsCalculatorImpl(new DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
                        new GaussianBasedTwdTransitionDistributionCache(modelStore, preloadAllModels,
                                modelCacheKeepAliveMillis),
                        propagateIntersectedWindRangeOfHeadupAndBearAway)));
    }

    public enum HmmTransitionProbabilitiesCalculator {
        INTERSECTED, SIMPLE, CLASSIFIER, GAUSSIAN_REGRESSOR
    }

}
