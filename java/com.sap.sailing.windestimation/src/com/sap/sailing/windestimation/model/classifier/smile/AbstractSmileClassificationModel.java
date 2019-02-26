package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.AbstractClassificationModel;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;

import smile.classification.SoftClassifier;
import smile.feature.Standardizer;
import smile.projection.PCA;

/**
 * Base class for all {@link TrainableClassificationModel} which are implemented using SMILE library. This base
 * implementation introduces full support for pre-processing by means of value scaling by Standardization and
 * dimensionality reduction with Principal Component Analysis (PCA). All this pre-processing methods require SMILE
 * library.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public abstract class AbstractSmileClassificationModel<InstanceType, MC extends ModelContext<InstanceType>>
        extends AbstractClassificationModel<InstanceType, MC> {

    private static final long serialVersionUID = 1037686504611915506L;

    private Standardizer scaler = null;
    private PCA pca = null;
    protected SoftClassifier<double[]> internalModel = null;

    public AbstractSmileClassificationModel(PreprocessingConfig preprocessingConfig, MC modelContext) {
        super(preprocessingConfig, modelContext);
    }

    @Override
    public void train(double[][] x, int[] y) {
        resetTrainingStats();
        PreprocessingConfig preprocessingConfig = getPreprocessingConfig();
        scaler = null;
        if (preprocessingConfig.isScaling()) {
            scaler = new Standardizer(false);
            scaler.learn(x);
            x = scaler.transform(x);
        }
        pca = null;
        if (preprocessingConfig.isPca()) {
            pca = new PCA(x);
            if (preprocessingConfig.isPcaComponents()) {
                pca.setProjection(preprocessingConfig.getNumberOfPcaComponents());
            } else if (preprocessingConfig.isPcaPercentage()) {
                pca.setProjection(preprocessingConfig.getPercentageValue());
            }
            x = pca.project(x);
        }
        internalModel = trainInternalModel(x, y);
        setModelAsReadyAfterSuccessfulTraining();
    }

    protected abstract SoftClassifier<double[]> trainInternalModel(double[][] x, int[] y);

    @Override
    public double[] classifyWithProbabilities(double[] x) {
        x = preprocessX(x);
        double[] likelihoods = new double[getModelContext().getNumberOfPossibleTargetValues()];
        internalModel.predict(x, likelihoods);
        return likelihoods;
    }

    protected double[] preprocessX(double[] x) {
        if (!isModelReady()) {
            throw new IllegalStateException("The classification model is not trained");
        }
        if (scaler != null) {
            x = scaler.transform(x);
        }
        if (pca != null) {
            x = pca.project(x);
        }
        return x;
    }

}
