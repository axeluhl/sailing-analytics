package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * Dummy pipeline which returns the same race instance as its output as provided by its input.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 *            Type of the input/output race
 */
public class DummyRacePreprocessingPipelineImpl<T> implements RacePreprocessingPipeline<T, T> {
    @Override
    public RaceWithEstimationData<T> preprocessInput(RaceWithEstimationData<T> race) {
        return race;
    }
}
