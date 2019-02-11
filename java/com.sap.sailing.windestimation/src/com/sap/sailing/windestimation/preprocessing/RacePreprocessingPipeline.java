package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * Specialized {@link PreprocessingPipeline} which imposes the type of input and output to be
 * {@link RaceWithEstimationData}. However, since {@link RaceWithEstimationData} has its elements defined by a generic
 * type, this type can be specified for input race and output race separately.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <FromElements>
 *            Type of elements of the input race
 * @param <ToElements>
 *            Type of elements of the output race
 * @see RaceWithEstimationData
 */
public interface RacePreprocessingPipeline<FromElements, ToElements>
        extends PreprocessingPipeline<RaceWithEstimationData<FromElements>, RaceWithEstimationData<ToElements>> {

    @Override
    RaceWithEstimationData<ToElements> preprocessInput(RaceWithEstimationData<FromElements> race);

}
