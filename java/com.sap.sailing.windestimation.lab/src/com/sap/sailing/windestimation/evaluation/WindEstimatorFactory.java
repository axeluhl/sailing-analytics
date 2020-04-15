package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.windestimation.WindEstimationComponent;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimatorFactory<T> {

    WindEstimationComponent<T> createNewEstimatorInstance();

}
