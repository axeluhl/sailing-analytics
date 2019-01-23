package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;

public class LabelledManeuverClassifierModelMetadata extends ManeuverClassifierModelMetadata
        implements LabelExtraction<ManeuverForEstimation> {

    private static final long serialVersionUID = -7074647974723150632L;

    public LabelledManeuverClassifierModelMetadata(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification... orderedSupportedTargetValues) {
        super(maneuverFeatures, boatClass, orderedSupportedTargetValues);
    }

    @Override
    public int getY(ManeuverForEstimation maneuver) {
        int y = indexToManeuverTypeOrdinalMapping[((LabelledManeuverForEstimation) maneuver).getManeuverType()
                .ordinal()];
        return y;
    }

}
