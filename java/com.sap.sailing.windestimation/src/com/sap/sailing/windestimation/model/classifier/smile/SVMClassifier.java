package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.math.kernel.GaussianKernel;

public class SVMClassifier<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public SVMClassifier(T modelContext) {
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

    @Override
    public boolean hasSupportForProvidedFeatures() {
        // return getModelContext().getBoatClass() != null;
        return true;
    }

}
