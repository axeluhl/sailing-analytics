package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ModelContext;

/**
 * Maps instance classifications to more suitable data structures.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are used by models operating in the context of this
 *            result mapper.
 * @param <MC>
 *            The type of model context associated with models which are used by models operating in the context of this
 *            result mapper.
 * @param <ResultType>
 *            The type of the result which will be returned to represent the model classification.
 */
public interface ClassificationResultMapper<InstanceType, MC extends ModelContext<InstanceType>, ResultType> {

    /**
     * Maps the classified instance to a more suitable data structure.
     * 
     * @param likelihoods
     *            Single-dimensional array with elements between 0.0 to 1.0 each representing the likelihood for a
     *            category y where y is the elements index. All values must sum up to 1.
     * @param instance
     *            The instance with features which were classified to produce the provided likelihoods.
     * @param modelContext
     *            The model context of the model which was used to classify the provided instance.
     * @return All the provided information wrapped within a single type.
     */
    ResultType mapToClassificationResult(double[] likelihoods, InstanceType instance, MC modelContext);

}
