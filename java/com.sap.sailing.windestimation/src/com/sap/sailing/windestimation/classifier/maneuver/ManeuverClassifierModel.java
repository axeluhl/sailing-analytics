package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.smile.NeuralNetworkManeuverClassifier;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierModel
        extends NeuralNetworkManeuverClassifier<ManeuverForEstimation, ManeuverModelMetadata> {

    private static final long serialVersionUID = -8752345937780750321L;

    public static final ManeuverTypeForInternalClassification[] orderedSupportedTargetValues = {
            ManeuverTypeForInternalClassification.TACK, ManeuverTypeForInternalClassification.JIBE,
            ManeuverTypeForInternalClassification.OTHER };

    public ManeuverClassifierModel(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        super(new ManeuverModelMetadata(maneuverFeatures, boatClass, orderedSupportedTargetValues));
    }

    public ManeuverClassifierModel(ManeuverFeatures maneuverFeatures) {
        super(new ManeuverModelMetadata(maneuverFeatures, null, orderedSupportedTargetValues));
    }

}
