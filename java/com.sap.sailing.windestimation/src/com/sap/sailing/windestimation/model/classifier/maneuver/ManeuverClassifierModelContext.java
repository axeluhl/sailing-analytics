package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.util.Arrays;

import com.sap.sailing.windestimation.aggregator.hmm.ProbabilityUtil;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

public class ManeuverClassifierModelContext extends ModelContext<ManeuverForEstimation> {

    private static final long serialVersionUID = -7074647974723150672L;
    private final ManeuverFeatures maneuverFeatures;
    private final String boatClassName;
    protected final int[] indexToManeuverTypeOrdinalMapping;
    private final int numberOfSupportedManeuverTypes;
    private final int otherTypes;

    public ManeuverClassifierModelContext(ManeuverFeatures maneuverFeatures, String boatClassName,
            ManeuverTypeForClassification... orderedSupportedTargetValues) {
        super(ModelDomainType.MANEUVER_CLASSIFIER);
        this.maneuverFeatures = maneuverFeatures;
        this.boatClassName = boatClassName;
        this.indexToManeuverTypeOrdinalMapping = new int[ManeuverTypeForClassification.values().length];
        for (int i = 0; i < indexToManeuverTypeOrdinalMapping.length; i++) {
            indexToManeuverTypeOrdinalMapping[i] = -1;
        }
        int i = 0;
        for (ManeuverTypeForClassification supportedManeuverType : orderedSupportedTargetValues) {
            indexToManeuverTypeOrdinalMapping[supportedManeuverType.ordinal()] = i++;
        }
        int others = 0;
        for (int j = 0; j < indexToManeuverTypeOrdinalMapping.length; j++) {
            if (indexToManeuverTypeOrdinalMapping[j] == -1) {
                indexToManeuverTypeOrdinalMapping[j] = i;
                others++;
            }
        }
        this.otherTypes = others;
        numberOfSupportedManeuverTypes = i + (others > 0 ? 1 : 0);
    }

    public ManeuverFeatures getManeuverFeatures() {
        return maneuverFeatures;
    }

    public String getBoatClassName() {
        return boatClassName;
    }

    @Override
    public int getNumberOfPossibleTargetValues() {
        return numberOfSupportedManeuverTypes;
    }

    public double[] getLikelihoodsPerManeuverTypeOrdinal(double[] likelihoodsFromModel) {
        double[] likelihoodsPerManeuverTypes = new double[ManeuverTypeForClassification.values().length];
        for (int j = 0; j < indexToManeuverTypeOrdinalMapping.length; j++) {
            int mappedI = indexToManeuverTypeOrdinalMapping[j];
            likelihoodsPerManeuverTypes[j] = likelihoodsFromModel[mappedI];
        }
        if (otherTypes > 1) {
            ProbabilityUtil.normalizeLikelihoodArray(likelihoodsPerManeuverTypes);
        }
        return likelihoodsPerManeuverTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boatClassName == null) ? 0 : boatClassName.hashCode());
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
        ManeuverClassifierModelContext other = (ManeuverClassifierModelContext) obj;
        if (boatClassName == null) {
            if (other.boatClassName != null)
                return false;
        } else if (!boatClassName.equals(other.boatClassName))
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
        return "ManeuverClassifierModelContext [maneuverFeatures=" + maneuverFeatures + ", boatClass=" + boatClassName
                + ", indexToManeuverTypeOrdinalMapping=" + Arrays.toString(indexToManeuverTypeOrdinalMapping)
                + ", numberOfSupportedManeuverTypes=" + numberOfSupportedManeuverTypes + "]";
    }

    public ManeuverTypeForClassification getManeuverTypeByMappingIndex(int likelihoodIndex) {
        for (ManeuverTypeForClassification maneuverType : ManeuverTypeForClassification.values()) {
            if (indexToManeuverTypeOrdinalMapping[maneuverType.ordinal()] == likelihoodIndex) {
                return maneuverType;
            }
        }
        return null;
    }

    public int getOtherTypes() {
        return otherTypes;
    }

    @Override
    public double[] getX(ManeuverForEstimation maneuver) {
        double[] inputVector = new double[getNumberOfInputFeatures()];
        int i = 0;
        inputVector[i++] = Math.abs(maneuver.getCourseChangeInDegrees());
        inputVector[i++] = maneuver.getSpeedLossRatio();
        inputVector[i++] = maneuver.getLowestSpeedVsExitingSpeedRatio();
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
        }
        return inputVector;
    }

    @Override
    public int getNumberOfInputFeatures() {
        int numberOfFeatures = 5;
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
        if (maneuverFeatures.isMarksInformation() && !maneuver.isMarkPassingDataAvailable()) {
            return false;
        }
        if (boatClassName != null && maneuver.getBoatClass() != null
                && !boatClassName.equals(maneuver.getBoatClass().getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String getId() {
        StringBuilder id = new StringBuilder("ManeuverClassification-");
        id.append(getManeuverFeatures().toString());
        id.append("-");
        if (getBoatClassName() == null) {
            id.append("All");
        } else {
            id.append(getBoatClassName());
        }
        return id.toString();
    }

}
