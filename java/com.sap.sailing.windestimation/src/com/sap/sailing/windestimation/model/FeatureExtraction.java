package com.sap.sailing.windestimation.model;

import java.util.List;

public interface FeatureExtraction<InstanceType> {

    double[] getX(InstanceType instance);

    default double[][] getXMatrix(List<? extends InstanceType> instances) {
        double[][] inputMatrix = new double[instances.size()][];
        int i = 0;
        for (InstanceType instance : instances) {
            inputMatrix[i++] = getX(instance);
        }
        return inputMatrix;
    }

    boolean isContainsAllFeatures(InstanceType instance);

    int getNumberOfInputFeatures();

}
