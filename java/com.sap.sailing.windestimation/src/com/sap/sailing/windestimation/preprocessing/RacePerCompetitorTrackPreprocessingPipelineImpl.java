package com.sap.sailing.windestimation.preprocessing;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class RacePerCompetitorTrackPreprocessingPipelineImpl implements
        PreprocessingPipeline<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>, List<RaceWithEstimationData<ManeuverForEstimation>>> {

    @Override
    public List<RaceWithEstimationData<ManeuverForEstimation>> preprocessRace(
            RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> race) {
        RaceWithEstimationData<ManeuverForEstimation> preprocessedRace = new RacePreprocessingPipelineImpl()
                .preprocessRace(race);
        List<RaceWithEstimationData<ManeuverForEstimation>> racesPerCompetitorTrack = preprocessedRace
                .getCompetitorTracks().stream()
                .map(competitorTrack -> preprocessedRace
                        .constructWithElements(Collections.singletonList(competitorTrack)))
                .collect(Collectors.toList());
        return racesPerCompetitorTrack;
    }

}
