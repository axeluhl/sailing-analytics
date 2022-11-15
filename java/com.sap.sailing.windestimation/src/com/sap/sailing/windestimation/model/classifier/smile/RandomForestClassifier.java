package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.RandomForest;

/**
 * Random Forest Classifier with 50 trees.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public class RandomForestClassifier<InstanceType, MC extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, MC> {

    private static final long serialVersionUID = -3364152319152090775L;

    public RandomForestClassifier(MC modelContext) {
        super(new PreprocessingConfigBuilder().build(), modelContext);
    }

    @Override
    protected RandomForest trainInternalModel(double[][] x, int[] y) {
        return new RandomForest(x, y, 50, getModelContext().getNumberOfInputFeatures());
    }

}
