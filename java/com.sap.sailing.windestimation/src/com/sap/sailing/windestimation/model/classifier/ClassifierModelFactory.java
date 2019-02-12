package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.ModelFactory;

/**
 * Specialized {@link ModelFactory} for classification models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are constructed by this factory instance.
 * @param <MC>
 *            The type of model context associated with models which are constructed by this factory instance.
 */
public interface ClassifierModelFactory<InstanceType, MC extends ModelContext<InstanceType>>
        extends ModelFactory<InstanceType, MC, TrainableClassificationModel<InstanceType, MC>> {

}
