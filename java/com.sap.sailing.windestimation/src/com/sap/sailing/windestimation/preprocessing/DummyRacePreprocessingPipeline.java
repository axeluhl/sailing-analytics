package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class DummyRacePreprocessingPipeline<T> implements RacePreprocessingPipeline<T, T> {
    @Override
    public RaceWithEstimationData<T> preprocessRace(RaceWithEstimationData<T> race) {
        return race;
    }
}
