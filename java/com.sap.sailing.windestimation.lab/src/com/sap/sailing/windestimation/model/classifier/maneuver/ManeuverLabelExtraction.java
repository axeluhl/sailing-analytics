package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.windestimation.data.LabeledManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;

public class ManeuverLabelExtraction implements LabelExtraction<LabeledManeuverForEstimation> {

    private final ManeuverClassifierModelContext modelContext;

    public ManeuverLabelExtraction(ManeuverClassifierModelContext modelContext) {
        this.modelContext = modelContext;
    }

    @Override
    public int getY(LabeledManeuverForEstimation maneuver) {
        int y = modelContext.indexToManeuverTypeOrdinalMapping[maneuver.getManeuverType().ordinal()];
        return y;
    }

}
