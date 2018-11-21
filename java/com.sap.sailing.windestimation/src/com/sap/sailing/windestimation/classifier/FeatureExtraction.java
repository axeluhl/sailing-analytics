package com.sap.sailing.windestimation.classifier;

import java.util.List;

public interface FeatureExtraction<InstanceType> {

    double[] getX(InstanceType instance);

    default double[][] getXMatrix(List<InstanceType> instances) {
        double[][] inputMatrix = new double[instances.size()][];
        int i = 0;
        for (InstanceType instance : instances) {
            inputMatrix[i++] = getX(instance);
        }
        return inputMatrix;
    }

    boolean isContainsAllFeatures(InstanceType instance);

    int getNumberOfInputFeatures();

    int getY(InstanceType maneuver);

    default int[] getYVector(List<InstanceType> instances) {
        int[] output = new int[instances.size()];
        int i = 0;
        for (InstanceType instance : instances) {
            output[i++] = getY(instance);
        }
        return output;
    }

}
