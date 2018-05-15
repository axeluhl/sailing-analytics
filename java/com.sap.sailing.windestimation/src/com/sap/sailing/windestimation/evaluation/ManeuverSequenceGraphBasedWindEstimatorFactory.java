package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.ManeuverAndPolarsBasedWindEstimator;
import com.sap.sailing.windestimation.ManeuverSequenceGraphBasedWindEstimatorImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraphBasedWindEstimatorFactory implements ManeuverAndPolarsBasedWindEstimatorFactory {

    private final PolarDataService polarService;

    public ManeuverSequenceGraphBasedWindEstimatorFactory(PolarDataService polarService) {
        this.polarService = polarService;
    }

    @Override
    public ManeuverAndPolarsBasedWindEstimator createNewEstimatorInstance() {
        return new ManeuverSequenceGraphBasedWindEstimatorImpl(polarService);
    }

}
