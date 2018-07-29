package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.windestimation.WindEstimator;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimatorFactory<T> {
    
    WindEstimator<T> createNewEstimatorInstance();

}
