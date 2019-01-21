package com.sap.sailing.windestimation.windinference;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.aggregator.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class PolarsBasedTwsCalculatorImpl implements TwsFromManeuverCalculator {

    private final PolarsFittingWindEstimation polarsFittingWindEstimation;

    public PolarsBasedTwsCalculatorImpl(PolarDataService polarService) {
        polarsFittingWindEstimation = new PolarsFittingWindEstimation(polarService);
    }

    @Override
    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        return polarsFittingWindEstimation.getWindSpeed(maneuver, windCourse);
    }
    
}
