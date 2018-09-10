package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.ManeuverClusteringBasedWindEstimatorImpl;
import com.sap.sailing.windestimation.ManeuverGraphBasedWindEstimatorImpl;
import com.sap.sailing.windestimation.WindEstimator;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimatorFactories {

    private final PolarDataService polarService;

    public WindEstimatorFactories(PolarDataService polarService) {
        this.polarService = polarService;
    }

    public WindEstimatorFactory<CompleteManeuverCurveWithEstimationData> maneuverGraph() {
        return new WindEstimatorFactory<CompleteManeuverCurveWithEstimationData>() {

            @Override
            public WindEstimator<CompleteManeuverCurveWithEstimationData> createNewEstimatorInstance() {
                return new ManeuverGraphBasedWindEstimatorImpl(polarService);
            }
        };
    }

    public WindEstimatorFactory<CompleteManeuverCurveWithEstimationData> maneuverClustering() {
        return new WindEstimatorFactory<CompleteManeuverCurveWithEstimationData>() {

            @Override
            public WindEstimator<CompleteManeuverCurveWithEstimationData> createNewEstimatorInstance() {
                return new ManeuverClusteringBasedWindEstimatorImpl(polarService);
            }
        };
    }

}
