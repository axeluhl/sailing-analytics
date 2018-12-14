package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.LabelExtraction;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierModelMetadata;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverTypeForInternalClassification;

public class LabelledManeuverModelMetadata extends ManeuverClassifierModelMetadata
        implements LabelExtraction<ManeuverForEstimation> {

    private static final long serialVersionUID = -7074647974723150632L;

    public LabelledManeuverModelMetadata(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForInternalClassification... orderedSupportedTargetValues) {
        super(maneuverFeatures, boatClass, orderedSupportedTargetValues);
    }

    @Override
    public int getY(ManeuverForEstimation maneuver) {
        int y = indexToManeuverTypeOrdinalMapping[((LabelledManeuverForEstimation) maneuver).getManeuverType()
                .ordinal()];
        return y;
    }

}
