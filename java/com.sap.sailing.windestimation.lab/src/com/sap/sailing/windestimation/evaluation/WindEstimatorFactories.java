package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.PolarsFittingBasedWindEstimationComponentImpl;
import com.sap.sailing.windestimation.SimpleConfigurableManeuverBasedWindEstimationComponentImpl;
import com.sap.sailing.windestimation.SimpleConfigurableManeuverBasedWindEstimationComponentImpl.ManeuverClassificationsAggregatorImplementation;
import com.sap.sailing.windestimation.WindEstimationComponent;
import com.sap.sailing.windestimation.aggregator.ManeuverClassificationsAggregatorFactory.HmmTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.transformer.CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.preprocessing.RaceElementsFilteringPreprocessingPipelineImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimatorFactories {

    private final PolarDataService polarService;
    private final ManeuverFeatures maneuverFeatures;
    private final ModelStore modelStore;

    public WindEstimatorFactories(PolarDataService polarService, ManeuverFeatures maneuverFeatures,
            ModelStore modelStore) {
        this.polarService = polarService;
        this.maneuverFeatures = maneuverFeatures;
        this.modelStore = modelStore;
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> get(
            EvaluatableWindEstimationImplementation windEstimationImplementation) {
        switch (windEstimationImplementation) {
        case HMM:
            return hmm(HmmTransitionProbabilitiesCalculator.INTERSECTED);
        case HMM_GAUSS:
            return hmm(HmmTransitionProbabilitiesCalculator.GAUSSIAN_REGRESSOR);
        case MST_HMM:
            return mstHmm();
        case CLUSTERING:
            return maneuverClustering();
        case MEAN_OUTLIER:
            return meanOutlierRemoval();
        case NEIGHBOR_OUTLIER:
            return neighborOutlierRemoval();
        case POLARS_FITTING:
            return polarsFitting();
        }
        throw new IllegalArgumentException(windEstimationImplementation + " is unsupported");
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> hmm(
            HmmTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        return new WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>>() {

            @Override
            public WindEstimationComponent<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> createNewEstimatorInstance() {
                return new SimpleConfigurableManeuverBasedWindEstimationComponentImpl(maneuverFeatures, modelStore,
                        polarService,
                        new RaceElementsFilteringPreprocessingPipelineImpl(
                                new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                        ManeuverClassificationsAggregatorImplementation.HMM, transitionProbabilitiesCalculator);
            }

            @Override
            public String toString() {
                return "HMM";
            }
        };
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> mstHmm() {
        return new WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>>() {

            @Override
            public WindEstimationComponent<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> createNewEstimatorInstance() {
                return new SimpleConfigurableManeuverBasedWindEstimationComponentImpl(maneuverFeatures, modelStore,
                        polarService,
                        new RaceElementsFilteringPreprocessingPipelineImpl(
                                new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                        ManeuverClassificationsAggregatorImplementation.MST_HMM, null);
            }

            @Override
            public String toString() {
                return "MST HMM";
            }
        };
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> maneuverClustering() {
        return new WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>>() {

            @Override
            public WindEstimationComponent<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> createNewEstimatorInstance() {
                return new SimpleConfigurableManeuverBasedWindEstimationComponentImpl(maneuverFeatures, modelStore,
                        polarService,
                        new RaceElementsFilteringPreprocessingPipelineImpl(
                                new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                        ManeuverClassificationsAggregatorImplementation.CLUSTERING, null);
            }

            @Override
            public String toString() {
                return "Maneuver Clustering";
            }
        };
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> polarsFitting() {
        return new WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>>() {

            @Override
            public WindEstimationComponent<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> createNewEstimatorInstance() {
                return new PolarsFittingBasedWindEstimationComponentImpl<>(
                        new RaceElementsFilteringPreprocessingPipelineImpl(
                                new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                        polarService);
            }

            @Override
            public String toString() {
                return "Polars Fitting";
            }
        };
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> meanOutlierRemoval() {
        return new WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>>() {

            @Override
            public WindEstimationComponent<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> createNewEstimatorInstance() {
                return new SimpleConfigurableManeuverBasedWindEstimationComponentImpl(maneuverFeatures, modelStore,
                        polarService,
                        new RaceElementsFilteringPreprocessingPipelineImpl(
                                new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                        ManeuverClassificationsAggregatorImplementation.MEAN_OUTLIER, null);
            }

            @Override
            public String toString() {
                return "Mean Outlier Removal";
            }
        };
    }

    public WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> neighborOutlierRemoval() {
        return new WindEstimatorFactory<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>>() {

            @Override
            public WindEstimationComponent<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> createNewEstimatorInstance() {
                return new SimpleConfigurableManeuverBasedWindEstimationComponentImpl(maneuverFeatures, modelStore,
                        polarService,
                        new RaceElementsFilteringPreprocessingPipelineImpl(
                                new CompleteManeuverCurveWithEstimationDataToManeuverForEstimationTransformer()),
                        ManeuverClassificationsAggregatorImplementation.NEIGHBOR_OUTLIER, null);
            }

            @Override
            public String toString() {
                return "Neighbor Outlier Removal";
            }
        };
    }
}
