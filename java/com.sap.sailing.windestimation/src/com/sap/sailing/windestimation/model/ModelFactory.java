package com.sap.sailing.windestimation.model;

import java.util.List;

import com.sap.sailing.windestimation.model.store.ModelDomainType;

/**
 * Factory for model construction.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are constructed by this factory instance.
 * @param <MC>
 *            The type of model context associated with models which are constructed by this factory instance.
 * @param <ModelType>
 *            The type of the models which are constructed by this factory instance.
 */
public interface ModelFactory<InstanceType, MC extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, MC>> {

    /**
     * Constructs an empty/untrained model for the provided model context.
     */
    ModelType getNewModel(MC modelContext);

    /**
     * Gets a list of all model contexts which manage a subset of features provided by
     * {@code modelContextWithMaxFeatures}.
     * 
     * @param modelContextWithMaxFeatures
     *            The model context defining the maximal superset of features to be used. The more features, the more
     *            models will with feature subsets will be included in the result.
     * @return A list with all model contexts containing feature subsets of the provided model context including the
     *         provided model context.
     * @see ModelLoader#loadBestModel(ModelContext)
     */
    List<MC> getAllCompatibleModelContexts(MC modelContextWithMaxFeatures);

    /**
     * Gets the domain type of models which are constructed by this model factory.
     */
    default ModelDomainType getModelDomainType() {
        return getModelContextWhichModelAreAlwaysPresent().getDomainType();
    }

    /**
     * Gets the model context which must be present in order to signal that the model cache managing the models of this
     * factory is ready.
     * 
     * @see ModelCache#isReady()
     */
    MC getModelContextWhichModelAreAlwaysPresent();

}
