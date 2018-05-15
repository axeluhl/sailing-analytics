package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.windestimation.ManeuverAndPolarsBasedWindEstimator;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverAndPolarsBasedWindEstimatorFactory {
    
    ManeuverAndPolarsBasedWindEstimator createNewEstimatorInstance();

}
