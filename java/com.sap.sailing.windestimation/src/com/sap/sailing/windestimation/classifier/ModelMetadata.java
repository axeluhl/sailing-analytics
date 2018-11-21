package com.sap.sailing.windestimation.classifier;

import java.io.Serializable;

public class ModelMetadata<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> implements Serializable {

    private static final long serialVersionUID = 6920073254335476342L;
    private final PreprocessingConfig preprocessingConfig;
    private final T contextSpecificModelMetadata;

    public ModelMetadata(PreprocessingConfig preprocessingConfig, T contextSpecificModelMetadata) {
        this.preprocessingConfig = preprocessingConfig;
        this.contextSpecificModelMetadata = contextSpecificModelMetadata;
    }

    public PreprocessingConfig getPreprocessingConfig() {
        return preprocessingConfig;
    }

    public T getContextSpecificModelMetadata() {
        return contextSpecificModelMetadata;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((contextSpecificModelMetadata == null) ? 0 : contextSpecificModelMetadata.hashCode());
        result = prime * result + ((preprocessingConfig == null) ? 0 : preprocessingConfig.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelMetadata other = (ModelMetadata) obj;
        if (contextSpecificModelMetadata == null) {
            if (other.contextSpecificModelMetadata != null)
                return false;
        } else if (!contextSpecificModelMetadata.equals(other.contextSpecificModelMetadata))
            return false;
        if (preprocessingConfig == null) {
            if (other.preprocessingConfig != null)
                return false;
        } else if (!preprocessingConfig.equals(other.preprocessingConfig))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ModelMetadata [preprocessingConfig=" + preprocessingConfig + ", contextSpecificModelMetadata="
                + contextSpecificModelMetadata + "]";
    }

}
