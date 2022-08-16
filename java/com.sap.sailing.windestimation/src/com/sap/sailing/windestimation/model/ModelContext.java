package com.sap.sailing.windestimation.model;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.store.AbstractModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

/**
 * Defines immutable context for a {@link TrainableModel}. A context can specify the feature set which must be extracted
 * from the input elements for associated model. Furthermore, it can also represent a value range of features which is
 * the model responsible for. It can also indicate whether a model must be trained using a particular partition within
 * the training data, e.g. maneuvers performed only by a boat of particular boat class. All this information must be
 * encoded by the context implementations accordingly to produce its unique context id. Each different context must
 * differ in terms of its {@link #getId()} and {@link #equals(Object)}. For each context, a corresponding model should
 * be trained if enough training data is available. A context must be derivable from an input instance used as input of
 * the model associated with this model context implementation.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models associated with this model context.
 */
public abstract class ModelContext<InstanceType> implements Serializable, FeatureExtraction<InstanceType> {

    private static final long serialVersionUID = 5069029031816423989L;
    private final ModelDomainType domainType;

    /**
     * Constructs a new instance of model context.
     * 
     * @param domainType
     *            The domain type associated with the model of this model context implementation.
     */
    public ModelContext(ModelDomainType domainType) {
        this.domainType = domainType;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    /**
     * Gets the number of possible values which can be predicted by the model.
     */
    public abstract int getNumberOfPossibleTargetValues();

    /**
     * Gets a unique id for this context. This id will be part of the persistence key of the model.
     * 
     * @see AbstractModelStoreImpl
     */
    public abstract String getId();

    /**
     * Gets the domain type associated with this model context implementation.
     */
    public ModelDomainType getDomainType() {
        return domainType;
    }

}