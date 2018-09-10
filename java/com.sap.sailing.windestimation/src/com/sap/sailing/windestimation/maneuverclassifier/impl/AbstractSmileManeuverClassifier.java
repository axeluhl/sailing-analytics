package com.sap.sailing.windestimation.maneuverclassifier.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.AbstractManeuverClassifier;
import com.sap.sailing.windestimation.maneuverclassifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.maneuverclassifier.MLUtil;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.PreprocessingConfig;
import com.sap.sailing.windestimation.maneuverclassifier.TrainableSingleManeuverOfflineClassifier;

import smile.classification.SoftClassifier;
import smile.feature.FeatureTransform;
import smile.feature.Standardizer;
import smile.projection.PCA;

public abstract class AbstractSmileManeuverClassifier<T extends SoftClassifier<double[]>>
        extends AbstractManeuverClassifier implements TrainableSingleManeuverOfflineClassifier {

    private static final long serialVersionUID = -8548600189459410079L;
    private T classifier = null;
    private double trainScore = 0;
    private double testScore = 0;
    private PCA pca = null;
    private FeatureTransform scaler = null;
    private final PreprocessingConfig preprocessingConfig;
    private int fixesCountForBoatClass = 0;

    public AbstractSmileManeuverClassifier(ManeuverFeatures maneuverFeatures, PreprocessingConfig preprocessingConfig,
            ManeuverTypeForClassification... supportedManeuverTypes) {
        this(maneuverFeatures, null, preprocessingConfig, supportedManeuverTypes);
    }

    public AbstractSmileManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            PreprocessingConfig preprocessingConfig, ManeuverTypeForClassification... supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, supportedManeuverTypes);
        this.preprocessingConfig = preprocessingConfig;
    }

    @SuppressWarnings("unchecked")
    public void loadPersistedModel() throws ClassifierPersistenceException {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(getFileForClassifier()))) {
            AbstractSmileManeuverClassifier<T> copy = (AbstractSmileManeuverClassifier<T>) input.readObject();
            this.classifier = copy.classifier;
            this.trainScore = copy.trainScore;
            this.testScore = copy.testScore;
            this.pca = copy.pca;
            this.scaler = copy.scaler;
            this.fixesCountForBoatClass = copy.fixesCountForBoatClass;
            if (getBoatClass() != copy.getBoatClass() && getBoatClass() != null && (copy.getBoatClass() == null
                    || !getBoatClass().getName().equals(copy.getBoatClass().getName()))) {
                throw new ClassifierPersistenceException("The boat class of the loaded classifier is: "
                        + copy.getBoatClass() + ". \nExpected: " + getBoatClass());
            }
            if (!getManeuverFeatures().equals(copy.getManeuverFeatures())) {
                throw new ClassifierPersistenceException("The maneuver features of the loaded classifier are: "
                        + copy.getManeuverFeatures() + ". \nExpected: " + getManeuverFeatures());
            }
            if (!Arrays.equals(getSupportedManeuverTypesMapping(), copy.getSupportedManeuverTypesMapping())) {
                throw new ClassifierPersistenceException("The supported maneuver types of the loaded classifier are: "
                        + copy.getSupportedManeuverTypes() + ". \nExpected: " + getSupportedManeuverTypes());
            }
            if (!preprocessingConfig.equals(copy.preprocessingConfig)) {
                throw new ClassifierPersistenceException("The preprocessing config of the loaded classifier are: "
                        + copy.preprocessingConfig + ". \nExpected: " + this.preprocessingConfig);
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    public void persistModel() throws ClassifierPersistenceException {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(getFileForClassifier()))) {
            output.writeObject(this);
        } catch (IOException e) {
            throw new ClassifierPersistenceException(e);
        }
    }

    private File getFileForClassifier() {
        String filename = "classifier_smile_" + getClass().getSimpleName() + "-" + getManeuverFeatures().toString()
                + "-" + (getBoatClass() == null ? "All" : getBoatClass().getName());
        return new File(filename);
    }

    @Override
    public void trainWithManeuvers(List<ManeuverForEstimation> maneuvers) {
        double[][] x = MLUtil.getInputMatrixAsDoubleArray(maneuvers, getManeuverFeatures());
        if (preprocessingConfig.isScaling()) {
            this.scaler = new Standardizer(false);
            scaler.learn(x);
            x = scaler.transform(x);
        }
        if (preprocessingConfig.isPca()) {
            this.pca = new PCA(x);
            if (preprocessingConfig.isPcaComponents()) {
                this.pca.setProjection(preprocessingConfig.getNumberOfPcaComponents());
            } else if (preprocessingConfig.isPcaPercentage()) {
                this.pca.setProjection(preprocessingConfig.getPercentageValue());
            }
            x = pca.project(x);
        }
        int[] y = MLUtil.getOutputAsIntArray(maneuvers, getSupportedManeuverTypesMapping());
        this.classifier = createTrainedClassifier(x, y);
    }

    protected abstract T createTrainedClassifier(double[][] x, int[] y);

    @Override
    public double[] classifyManeuverWithProbabilities(ManeuverForEstimation maneuver) {
        if (classifier == null) {
            throw new IllegalStateException(
                    "The classifier is not initialised. You have either to train it, or to load it from persistence.");
        }
        double[] x = MLUtil.getInputVectorAsDoubleArray(maneuver, getManeuverFeatures());
        if (scaler != null) {
            x = scaler.transform(x);
        }
        if (pca != null) {
            x = pca.project(x);
        }
        double[] posteriori = new double[getSupportedManeuverTypesCount()];
        classifier.predict(x, posteriori);
        double[] posterioriPerManeuverType = new double[ManeuverTypeForClassification.values().length];
        int mappedI = 0;
        int[] supportedManeuverTypesMapping = getSupportedManeuverTypesMapping();
        for (int i = 0; i < posterioriPerManeuverType.length; i++) {
            int maneuverTypeMapping = supportedManeuverTypesMapping[i];
            if (maneuverTypeMapping >= 0) {
                posterioriPerManeuverType[i] = posteriori[mappedI++];
            }
        }
        return posterioriPerManeuverType;
    }

    @Override
    public double getTestScore() {
        return testScore;
    }

    @Override
    public void setTestScore(double testScore) {
        this.testScore = testScore;
    }

    @Override
    public double getTrainScore() {
        return trainScore;
    }

    @Override
    public void setTrainScore(double trainScore) {
        this.trainScore = trainScore;
    }

    @Override
    public int getFixesCountForBoatClass() {
        return fixesCountForBoatClass;
    }

    @Override
    public void setFixesCountForBoatClass(int fixesCountForBoatClass) {
        this.fixesCountForBoatClass = fixesCountForBoatClass;
    }

    @Override
    public boolean hasSupportForProvidedFeatures() {
        return true;
    }

}
