package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.ManeuverTypeGraphBasedWindEstimatorImpl;
import com.sap.sailing.windestimation.WindEstimator;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraphBasedWindEstimatorFactory
        implements WindEstimatorFactory<CompleteManeuverCurveWithEstimationData> {

    private final PolarDataService polarService;

    public ManeuverSequenceGraphBasedWindEstimatorFactory(PolarDataService polarService) {
        this.polarService = polarService;
    }

    @Override
    public WindEstimator<CompleteManeuverCurveWithEstimationData> createNewEstimatorInstance() {
        return new ManeuverTypeGraphBasedWindEstimatorImpl(polarService);
    }

}
