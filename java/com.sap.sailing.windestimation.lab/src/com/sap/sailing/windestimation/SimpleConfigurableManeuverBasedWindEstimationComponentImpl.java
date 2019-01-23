package com.sap.sailing.windestimation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.ManeuverClassificationsAggregatorFactory.HmmTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.transformer.CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.preprocessing.RaceElementsFilteringPreprocessingPipelineImpl;
import com.sap.sailing.windestimation.preprocessing.RacePreprocessingPipeline;
import com.sap.sailing.windestimation.windinference.DummyBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;
import com.sap.sailing.windestimation.windinference.PolarsBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.WindTrackCalculatorImpl;

public class SimpleConfigurableManeuverBasedWindEstimationComponentImpl extends
        ManeuverBasedWindEstimationComponentImpl<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> {

    private static final int MODEL_CACHE_KEEP_ALIVE_MILLIS = 3600000;
    private static final HmmTransitionProbabilitiesCalculator transitionProbabilitiesCalculatorType = HmmTransitionProbabilitiesCalculator.INTERSECTED;
    private static final boolean propagateIntersectedWindRangeOfHeadupAndBearAway = false;

    public SimpleConfigurableManeuverBasedWindEstimationComponentImpl(ManeuverFeatures maneuverFeatures,
            ModelStore modelStore, PolarDataService polarService,
            RacePreprocessingPipeline<CompleteManeuverCurveWithEstimationData, ManeuverForEstimation> preprocessingPipeline,
            ManeuverClassificationsAggregatorImplementation aggregatorImplementation) {
        super(preprocessingPipeline,
                new ManeuverClassifiersCache(modelStore, polarService, MODEL_CACHE_KEEP_ALIVE_MILLIS, maneuverFeatures),
                aggregatorImplementation.createNewInstance(polarService, modelStore, MODEL_CACHE_KEEP_ALIVE_MILLIS),
                new WindTrackCalculatorImpl(new MiddleCourseBasedTwdCalculatorImpl(),
                        maneuverFeatures.isPolarsInformation() ? new PolarsBasedTwsCalculatorImpl(polarService)
                                : new DummyBasedTwsCalculatorImpl()));
    }

    public SimpleConfigurableManeuverBasedWindEstimationComponentImpl(ManeuverFeatures maneuverFeatures,
            ModelStore modelStore, PolarDataService polarService,
            ManeuverClassificationsAggregatorImplementation aggregatorImplementation) {
        this(maneuverFeatures, modelStore, polarService,
                new RaceElementsFilteringPreprocessingPipelineImpl(
                        new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                aggregatorImplementation);
    }

    public SimpleConfigurableManeuverBasedWindEstimationComponentImpl(ManeuverFeatures maneuverFeatures,
            ModelStore modelStore, PolarDataService polarService) {
        this(maneuverFeatures, modelStore, polarService, ManeuverClassificationsAggregatorImplementation.HMM);
    }

    public enum ManeuverClassificationsAggregatorImplementation {
        HMM, MST_HMM, CLUSTERING, MEAN_OUTLIER, NEIGHBOR_OUTLIER;

        ManeuverClassificationsAggregator createNewInstance(PolarDataService polarService, ModelStore modelStore,
                long modelCacheKeepAliveMillis) {
            ManeuverClassificationsAggregatorFactory factory = new ManeuverClassificationsAggregatorFactory(
                    polarService, modelStore, modelCacheKeepAliveMillis);
            switch (this) {
            case HMM:
                return factory.hmm(transitionProbabilitiesCalculatorType,
                        propagateIntersectedWindRangeOfHeadupAndBearAway);
            case MST_HMM:
                return factory.mstHmm(propagateIntersectedWindRangeOfHeadupAndBearAway);
            case CLUSTERING:
                return factory.clustering();
            case MEAN_OUTLIER:
                return factory.meanOutlier();
            case NEIGHBOR_OUTLIER:
                return factory.neighborOutlier();
            }
            throw new IllegalArgumentException(this + " implementation type is unsupported");
        }
    }

}
