package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;

public abstract class AbstractManeuverClassifier implements ManeuverClassifier, SingleManeuverClassifier {

    private final ManeuverFeatures maneuverFeatures;
    private final BoatClass boatClass;
    private final int[] supportedManeuverTypesMapping;
    private final int supportedManeuverTypesCount;

    public AbstractManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        this.maneuverFeatures = maneuverFeatures;
        this.boatClass = boatClass;
        this.supportedManeuverTypesMapping = new int[ManeuverTypeForClassification.values().length];
        for (int i = 0; i < supportedManeuverTypes.length; i++) {
            supportedManeuverTypesMapping[i] = -1;
        }
        int i = 0;
        for (ManeuverTypeForClassification supportedManeuverType : supportedManeuverTypes) {
            supportedManeuverTypesMapping[supportedManeuverType.ordinal()] = i++;
        }
        this.supportedManeuverTypesCount = i;
    }

    @Override
    public ManeuverEstimationResult classifyManeuver(ManeuverForEstimation maneuver) {
        double[] likelihoodPerManeuverType = classifyManeuverWithProbabilities(maneuver);
        ManeuverEstimationResult maneuverClassificationResult = new ManeuverEstimationResult(maneuver,
                likelihoodPerManeuverType);
        return maneuverClassificationResult;
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
    public boolean isSupportsManeuverType(ManeuverTypeForClassification maneuverType) {
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
    public List<ManeuverTypeForClassification> getSupportedManeuverTypes() {
        List<ManeuverTypeForClassification> supportedManeuverTypes = new ArrayList<>();
        for (ManeuverTypeForClassification maneuverType : ManeuverTypeForClassification.values()) {
            if (supportedManeuverTypesMapping[maneuverType.ordinal()] >= 0) {
                supportedManeuverTypes.add(maneuverType);
            }
        }
        return supportedManeuverTypes;
    }

    @Override
    public ManeuverTypeForClassification getManeuverTypeByMappingIndex(int mappingIndex) {
        for (ManeuverTypeForClassification maneuverType : ManeuverTypeForClassification.values()) {
            if (supportedManeuverTypesMapping[maneuverType.ordinal()] == mappingIndex) {
                return maneuverType;
            }
        }
        return null;
    }

}
