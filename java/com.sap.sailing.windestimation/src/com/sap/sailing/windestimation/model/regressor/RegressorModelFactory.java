package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.ModelFactory;

/**
 * Specialized {@link ModelFactory} for regression models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are constructed by this factory instance.
 * @param <MC>
 *            The type of model context associated with models which are constructed by this factory instance.
 */
public interface RegressorModelFactory<InstanceType, MC extends ModelContext<InstanceType>>
        extends ModelFactory<InstanceType, MC, TrainableRegressorModel<InstanceType, MC>> {

}
