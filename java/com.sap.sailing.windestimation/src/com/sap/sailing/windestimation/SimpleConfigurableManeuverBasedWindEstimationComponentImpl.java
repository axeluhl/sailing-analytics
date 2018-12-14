package com.sap.sailing.windestimation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.transformer.ManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.preprocessing.RaceElementsFilteringPreprocessingPipelineImpl;
import com.sap.sailing.windestimation.preprocessing.RacePreprocessingPipeline;
import com.sap.sailing.windestimation.windinference.DummyBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.MiddleCourseBasedTwdCalculatorImpl;
import com.sap.sailing.windestimation.windinference.PolarsBasedTwsCalculatorImpl;
import com.sap.sailing.windestimation.windinference.WindTrackCalculatorImpl;

public class SimpleConfigurableManeuverBasedWindEstimationComponentImpl extends
        ManeuverBasedWindEstimationComponentImpl<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> {

    public SimpleConfigurableManeuverBasedWindEstimationComponentImpl(ManeuverFeatures maneuverFeatures,
            ManeuverClassifiersCache maneuverClassifiersCache, PolarDataService polarService,
            RacePreprocessingPipeline<CompleteManeuverCurveWithEstimationData, ManeuverForEstimation> preprocessingPipeline,
            ManeuverClassificationsAggregatorImplementation aggregatorImplementation) {
        super(preprocessingPipeline, maneuverClassifiersCache, aggregatorImplementation.createNewInstance(polarService),
                new WindTrackCalculatorImpl(new MiddleCourseBasedTwdCalculatorImpl(),
                        maneuverFeatures.isPolarsInformation() ? new PolarsBasedTwsCalculatorImpl(polarService)
                                : new DummyBasedTwsCalculatorImpl()));
    }

    public SimpleConfigurableManeuverBasedWindEstimationComponentImpl(ManeuverFeatures maneuverFeatures,
            ManeuverClassifiersCache maneuverClassifiersCache, PolarDataService polarService,
            ManeuverClassificationsAggregatorImplementation aggregatorImplementation) {
        this(maneuverFeatures, maneuverClassifiersCache, polarService,
                new RaceElementsFilteringPreprocessingPipelineImpl(new ManeuverForEstimationTransformer()),
                aggregatorImplementation);
    }

    public SimpleConfigurableManeuverBasedWindEstimationComponentImpl(ManeuverFeatures maneuverFeatures,
            ManeuverClassifiersCache maneuverClassifiersCache, PolarDataService polarService) {
        this(maneuverFeatures, maneuverClassifiersCache, polarService,
                ManeuverClassificationsAggregatorImplementation.HMM);
    }

    public enum ManeuverClassificationsAggregatorImplementation {
        HMM, CLUSTERING, MEAN_OUTLIER, NEIGHBOR_OUTLIER;

        ManeuverClassificationsAggregator createNewInstance(PolarDataService polarService) {
            ManeuverClassificationsAggregatorFactory factory = new ManeuverClassificationsAggregatorFactory(
                    polarService);
            switch (this) {
            case HMM:
                return factory.hmm();
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
