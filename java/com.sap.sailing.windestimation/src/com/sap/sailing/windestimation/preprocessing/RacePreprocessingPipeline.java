package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public interface RacePreprocessingPipeline<FromElements, ToElements>
        extends PreprocessingPipeline<RaceWithEstimationData<FromElements>, RaceWithEstimationData<ToElements>> {

    RaceWithEstimationData<ToElements> preprocessRace(RaceWithEstimationData<FromElements> race);

}
