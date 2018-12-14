package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.util.Arrays;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ContextType;

public class ManeuverClassifierModelMetadata extends ContextSpecificModelMetadata<ManeuverForEstimation> {

    private static final long serialVersionUID = -7074647974723150672L;
    private final ManeuverFeatures maneuverFeatures;
    private final BoatClass boatClass;
    protected final int[] indexToManeuverTypeOrdinalMapping;
    private final int numberOfSupportedManeuverTypes;

    public ManeuverClassifierModelMetadata(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForInternalClassification... orderedSupportedTargetValues) {
        super(ContextType.MANEUVER);
        this.maneuverFeatures = maneuverFeatures;
        this.boatClass = boatClass;
        this.indexToManeuverTypeOrdinalMapping = new int[ManeuverTypeForInternalClassification.values().length];
        for (int i = 0; i < indexToManeuverTypeOrdinalMapping.length; i++) {
            indexToManeuverTypeOrdinalMapping[i] = -1;
        }
        int i = 0;
        for (ManeuverTypeForInternalClassification supportedManeuverType : orderedSupportedTargetValues) {
            indexToManeuverTypeOrdinalMapping[supportedManeuverType.ordinal()] = i++;
        }
        numberOfSupportedManeuverTypes = i;
    }

    public ManeuverFeatures getManeuverFeatures() {
        return maneuverFeatures;
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public int getNumberOfPossibleTargetValues() {
        return numberOfSupportedManeuverTypes;
    }

    public double[] getLikelihoodsPerManeuverTypeOrdinal(double[] likelihoodsFromModel) {
        double[] likelihoodsPerManeuverTypes = new double[ManeuverTypeForInternalClassification.values().length];
        int mappedI = 0;
        for (int i = 0; i < likelihoodsPerManeuverTypes.length; i++) {
            int maneuverTypeMapping = indexToManeuverTypeOrdinalMapping[i];
            if (maneuverTypeMapping >= 0) {
                likelihoodsPerManeuverTypes[i] = likelihoodsFromModel[mappedI++];
            }
        }
        return likelihoodsPerManeuverTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
        result = prime * result + Arrays.hashCode(indexToManeuverTypeOrdinalMapping);
        result = prime * result + ((maneuverFeatures == null) ? 0 : maneuverFeatures.hashCode());
        result = prime * result + numberOfSupportedManeuverTypes;
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
        ManeuverClassifierModelMetadata other = (ManeuverClassifierModelMetadata) obj;
        if (boatClass == null) {
            if (other.boatClass != null)
                return false;
        } else if (!boatClass.equals(other.boatClass))
            return false;
        if (!Arrays.equals(indexToManeuverTypeOrdinalMapping, other.indexToManeuverTypeOrdinalMapping))
            return false;
        if (maneuverFeatures == null) {
            if (other.maneuverFeatures != null)
                return false;
        } else if (!maneuverFeatures.equals(other.maneuverFeatures))
            return false;
        if (numberOfSupportedManeuverTypes != other.numberOfSupportedManeuverTypes)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ManeuverClassifierModelMetadata [maneuverFeatures=" + maneuverFeatures + ", boatClass=" + boatClass
                + ", indexToManeuverTypeOrdinalMapping=" + Arrays.toString(indexToManeuverTypeOrdinalMapping)
                + ", numberOfSupportedManeuverTypes=" + numberOfSupportedManeuverTypes + "]";
    }

    public ManeuverTypeForInternalClassification getManeuverTypeByMappingIndex(int likelihoodIndex) {
        for (ManeuverTypeForInternalClassification maneuverType : ManeuverTypeForInternalClassification.values()) {
            if (indexToManeuverTypeOrdinalMapping[maneuverType.ordinal()] == likelihoodIndex) {
                return maneuverType;
            }
        }
        return null;
    }

    @Override
    public double[] getX(ManeuverForEstimation maneuver) {
        double[] inputVector = new double[getNumberOfInputFeatures()];
        int i = 0;
        inputVector[i++] = Math.abs(maneuver.getCourseChangeInDegrees());
        inputVector[i++] = maneuver.getSpeedLossRatio();
        inputVector[i++] = maneuver.getSpeedGainRatio();
        inputVector[i++] = maneuver.getMaxTurningRateInDegreesPerSecond();
        if (maneuverFeatures.isPolarsInformation()) {
            inputVector[i++] = maneuver.getDeviationFromOptimalTackAngleInDegrees();
            inputVector[i++] = maneuver.getDeviationFromOptimalJibeAngleInDegrees();
        }
        if (maneuverFeatures.isScaledSpeed()) {
            inputVector[i++] = maneuver.getScaledSpeedBefore();
            inputVector[i++] = maneuver.getScaledSpeedAfter();
        }
        if (maneuverFeatures.isMarksInformation()) {
            inputVector[i++] = maneuver.isMarkPassing() ? 1.0 : 0.0;
            // inputVector[i++] = maneuver.getRelativeBearingToNextMarkBefore();
            // inputVector[i++] = maneuver.getRelativeBearingToNextMarkAfter();
        }
        return inputVector;
    }

    @Override
    public int getNumberOfInputFeatures() {
        int numberOfFeatures = 4;
        if (maneuverFeatures.isPolarsInformation()) {
            numberOfFeatures += 2;
        }
        if (maneuverFeatures.isScaledSpeed()) {
            numberOfFeatures += 2;
        }
        if (maneuverFeatures.isMarksInformation()) {
            numberOfFeatures += 1;
        }
        return numberOfFeatures;
    }

    @Override
    public boolean isContainsAllFeatures(ManeuverForEstimation maneuver) {
        if (maneuverFeatures.isPolarsInformation()) {
            if (maneuver.getDeviationFromOptimalJibeAngleInDegrees() == null
                    || maneuver.getDeviationFromOptimalTackAngleInDegrees() == null) {
                return false;
            }
        }
        if (maneuverFeatures.isMarksInformation()) {
            if (maneuver.getRelativeBearingToNextMarkBefore() == null
                    || maneuver.getRelativeBearingToNextMarkAfter() == null) {
                return false;
            }
        }
        if (boatClass != null && (maneuver.getBoatClass() == null
                || !boatClass.getName().equals(maneuver.getBoatClass().getName()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getId() {
        StringBuilder id = new StringBuilder("ManeuverClassification-");
        id.append(getManeuverFeatures().toString());
        id.append("-");
        if (getBoatClass() == null) {
            id.append("All");
        } else {
            id.append(getBoatClass().getName());
            id.append("_");
            id.append(getBoatClass().typicallyStartsUpwind() ? "startsUpwind" : "startsDownwind");
        }
        return id.toString();
    }

}
