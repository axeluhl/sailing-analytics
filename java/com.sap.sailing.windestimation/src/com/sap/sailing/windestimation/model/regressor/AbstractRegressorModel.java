package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.AbstractTrainableModel;
import com.sap.sailing.windestimation.model.ModelContext;

/**
 * Base class for classification models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public abstract class AbstractRegressorModel<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractTrainableModel<InstanceType, T> implements TrainableRegressorModel<InstanceType, T> {

    private static final long serialVersionUID = -3283338628316L;

    public AbstractRegressorModel(T modelContext) {
        super(modelContext);
    }

}
