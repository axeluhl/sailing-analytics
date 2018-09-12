package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.ManeuverClusteringBasedWindEstimatorImpl;
import com.sap.sailing.windestimation.ManeuverGraphBasedWindEstimatorImpl;
import com.sap.sailing.windestimation.PolarsFittingBasedWindEstimatorImpl;
import com.sap.sailing.windestimation.WindEstimator;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimatorFactories {

    private final PolarDataService polarService;
    private final ManeuverFeatures maneuverFeatures;

    public WindEstimatorFactories(PolarDataService polarService, ManeuverFeatures maneuverFeatures) {
        this.polarService = polarService;
        this.maneuverFeatures = maneuverFeatures;
    }

    public WindEstimatorFactory<CompleteManeuverCurveWithEstimationData> maneuverGraph() {
        return new WindEstimatorFactory<CompleteManeuverCurveWithEstimationData>() {

            @Override
            public WindEstimator<CompleteManeuverCurveWithEstimationData> createNewEstimatorInstance() {
                return new ManeuverGraphBasedWindEstimatorImpl(polarService, maneuverFeatures);
            }
        };
    }

    public WindEstimatorFactory<CompleteManeuverCurveWithEstimationData> maneuverClustering() {
        return new WindEstimatorFactory<CompleteManeuverCurveWithEstimationData>() {

            @Override
            public WindEstimator<CompleteManeuverCurveWithEstimationData> createNewEstimatorInstance() {
                return new ManeuverClusteringBasedWindEstimatorImpl(polarService, maneuverFeatures);
            }
        };
    }

    public WindEstimatorFactory<CompleteManeuverCurveWithEstimationData> polarsFitting() {
        return new WindEstimatorFactory<CompleteManeuverCurveWithEstimationData>() {

            @Override
            public WindEstimator<CompleteManeuverCurveWithEstimationData> createNewEstimatorInstance() {
                return new PolarsFittingBasedWindEstimatorImpl(polarService);
            }
        };
    }

}
