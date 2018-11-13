package com.sap.sailing.windestimation.maneuverclassifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public abstract class AbstractManeuverClassifier implements ProbabilisticManeuverClassifier, Serializable {

    private static final long serialVersionUID = 908308394572292148L;
    private final ManeuverFeatures maneuverFeatures;
    private final BoatClass boatClass;
    private final int[] supportedManeuverTypesMapping;
    private final int supportedManeuverTypesCount;

    public AbstractManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForInternalClassification[] supportedManeuverTypes) {
        this.maneuverFeatures = maneuverFeatures;
        this.boatClass = boatClass;
        this.supportedManeuverTypesMapping = new int[ManeuverTypeForInternalClassification.values().length];
        for (int i = 0; i < supportedManeuverTypes.length; i++) {
            supportedManeuverTypesMapping[i] = -1;
        }
        int i = 0;
        for (ManeuverTypeForInternalClassification supportedManeuverType : supportedManeuverTypes) {
            supportedManeuverTypesMapping[supportedManeuverType.ordinal()] = i++;
        }
        this.supportedManeuverTypesCount = i;
    }

    @Override
    public ManeuverWithProbabilisticTypeClassification classifyManeuver(ManeuverForEstimation maneuver) {
        double[] likelihoodPerManeuverType = classifyManeuverWithProbabilities(maneuver);
        double[] likelihoodPerCoarseGrainedManeuverType = mapManeuverTypesToCoarseGrainedManeuverTypes(
                likelihoodPerManeuverType, maneuver.getManeuverCategory());
        ManeuverWithProbabilisticTypeClassification maneuverClassificationResult = new ManeuverWithProbabilisticTypeClassification(maneuver,
                likelihoodPerCoarseGrainedManeuverType);
        return maneuverClassificationResult;
    }

    private double[] mapManeuverTypesToCoarseGrainedManeuverTypes(double[] likelihoodPerManeuverType,
            ManeuverCategory maneuverCategory) {
        double[] newLikelihoods = new double[ManeuverTypeForClassification.values().length];
        switch (maneuverCategory) {
        case MARK_PASSING:
        case REGULAR:
            newLikelihoods[ManeuverTypeForClassification.TACK
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.TACK.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.JIBE
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.JIBE.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.BEAR_AWAY
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.OTHER.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.HEAD_UP
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.OTHER.ordinal()];
            break;
        default:
            break;
        }
        return newLikelihoods;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public ManeuverFeatures getManeuverFeatures() {
        return maneuverFeatures;
    }

    @Override
    public boolean isSupportsManeuverType(ManeuverTypeForInternalClassification maneuverType) {
        return supportedManeuverTypesMapping[maneuverType.ordinal()] >= 0;
    }

    @Override
    public int[] getSupportedManeuverTypesMapping() {
        return supportedManeuverTypesMapping;
    }

    @Override
    public int getSupportedManeuverTypesCount() {
        return supportedManeuverTypesCount;
    }

    @Override
    public List<ManeuverTypeForInternalClassification> getSupportedManeuverTypes() {
        List<ManeuverTypeForInternalClassification> supportedManeuverTypes = new ArrayList<>();
        for (ManeuverTypeForInternalClassification maneuverType : ManeuverTypeForInternalClassification.values()) {
            if (supportedManeuverTypesMapping[maneuverType.ordinal()] >= 0) {
                supportedManeuverTypes.add(maneuverType);
            }
        }
        return supportedManeuverTypes;
    }

    @Override
    public ManeuverTypeForInternalClassification getManeuverTypeByMappingIndex(int mappingIndex) {
        for (ManeuverTypeForInternalClassification maneuverType : ManeuverTypeForInternalClassification.values()) {
            if (supportedManeuverTypesMapping[maneuverType.ordinal()] == mappingIndex) {
                return maneuverType;
            }
        }
        return null;
    }

}
