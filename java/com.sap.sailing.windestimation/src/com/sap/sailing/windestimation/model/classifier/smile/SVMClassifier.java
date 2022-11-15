package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.math.kernel.GaussianKernel;

/**
 * Support Vector Machines (SVM) Classifier with gaussian kernel. Please note that due to quadratic complexity for
 * training of this model, this classifier cannot be trained with more than 50 000 instances within a reasonable time.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public class SVMClassifier<InstanceType, MC extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, MC> {

    private static final long serialVersionUID = -3364152319152090775L;

    public SVMClassifier(MC modelContext) {
        super(new PreprocessingConfigBuilder().scaling().build(), modelContext);
    }

    @Override
    protected SVM<double[]> trainInternalModel(double[][] x, int[] y) {
        int featuresCount = x[0].length;
        double gamma = 1.0 / featuresCount;
        double sigma = Math.sqrt(0.5 / gamma);
        SVM<double[]> svm = new SVM<>(new GaussianKernel(sigma), 10.0,
                getModelContext().getNumberOfPossibleTargetValues(), Multiclass.ONE_VS_ALL);
        svm.learn(x, y);
        svm.finish();
        svm.trainPlattScaling(x, y);
        return svm;
    }

}
