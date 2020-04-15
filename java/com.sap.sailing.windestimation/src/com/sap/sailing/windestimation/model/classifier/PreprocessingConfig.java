package com.sap.sailing.windestimation.model.classifier;

import java.io.Serializable;

/**
 * Represents pre-processing configuration of a model. The pre-processing can include feature scaling with
 * Standardization and dimensionality reduction with Principal Component Analysis (PCA). The configured pre-processing
 * must be applied on the input feature vector before it will be forwarded to the model for training or predicting.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PreprocessingConfig implements Serializable {

    private static final long serialVersionUID = 1468758171182350224L;

    private final boolean pcaComponents;
    private final boolean pcaPercentage;
    private final double pcaValue;
    private final boolean scaling;

    public PreprocessingConfig(boolean pcaComponents, boolean pcaPercentage, double pcaValue, boolean scaling) {
        this.pcaComponents = pcaComponents;
        this.pcaPercentage = pcaPercentage;
        this.pcaValue = pcaValue;
        this.scaling = scaling;
    }

    public boolean isPca() {
        return pcaComponents || pcaPercentage;
    }

    public boolean isPcaComponents() {
        return pcaComponents;
    }

    public int getNumberOfPcaComponents() {
        return (int) pcaValue;
    }

    public double getPercentageValue() {
        return pcaValue;
    }

    public boolean isPcaPercentage() {
        return pcaPercentage;
    }

    public boolean isScaling() {
        return scaling;
    }

    @Override
    public String toString() {
        return "PreprocessingConfig [pcaComponents=" + pcaComponents + ", pcaPercentage=" + pcaPercentage
                + ", pcaValue=" + pcaValue + ", scaling=" + scaling + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (pcaComponents ? 1231 : 1237);
        result = prime * result + (pcaPercentage ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(pcaValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (scaling ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PreprocessingConfig other = (PreprocessingConfig) obj;
        if (pcaComponents != other.pcaComponents)
            return false;
        if (pcaPercentage != other.pcaPercentage)
            return false;
        if (Double.doubleToLongBits(pcaValue) != Double.doubleToLongBits(other.pcaValue))
            return false;
        if (scaling != other.scaling)
            return false;
        return true;
    }

    public static class PreprocessingConfigBuilder {
        private boolean pcaComponents;
        private boolean pcaPercentage;
        private double pcaValue;
        private boolean scaling;

        public PreprocessingConfigBuilder pca(int pcaComponents) {
            this.pcaComponents = true;
            this.pcaPercentage = false;
            this.pcaValue = pcaComponents;
            return this;
        }

        public PreprocessingConfigBuilder pca(double pcaPercentageValue) {
            this.pcaPercentage = true;
            this.pcaComponents = false;
            this.pcaValue = pcaPercentageValue;
            return this;
        }

        public PreprocessingConfigBuilder scaling() {
            this.scaling = true;
            return this;
        }

        public PreprocessingConfig build() {
            return new PreprocessingConfig(pcaComponents, pcaPercentage, pcaValue, scaling);
        }
    }
}
