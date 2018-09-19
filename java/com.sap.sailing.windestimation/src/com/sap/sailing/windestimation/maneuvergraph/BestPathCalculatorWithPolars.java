package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class BestPathCalculatorWithPolars extends BestPathsCalculator {

    private final PolarsFittingWindEstimation polarsFittingWindEstimation;

    public BestPathCalculatorWithPolars(PolarsFittingWindEstimation polarsFittingWindEstimation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(transitionProbabilitiesCalculator);
        this.polarsFittingWindEstimation = polarsFittingWindEstimation;
    }

    @Override
    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return polarsFittingWindEstimation.getWindSpeed(maneuver, windCourse);
    }

}
