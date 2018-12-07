package com.sap.sailing.windestimation.preprocessing;

import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.transformer.ManeuverForEstimationTransformer;

public class RaceElementsFilteringPreprocessingPipelineImpl
        implements RacePreprocessingPipeline<CompleteManeuverCurveWithEstimationData, ManeuverForEstimation> {

    @Override
    public RaceWithEstimationData<ManeuverForEstimation> preprocessRace(
            RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> race) {
        ManeuverForEstimationTransformer transformer = new ManeuverForEstimationTransformer();
        ManeuverFilteringImpl maneuverFiltering = new ManeuverFilteringImpl();
        List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks = race.getCompetitorTracks()
                .stream().filter(new CompetitorTrackFilteringImpl<>())
                .map(competitorTrack -> competitorTrack.constructWithElements(transformer.apply(competitorTrack)
                        .getElements().stream().filter(maneuverFiltering).collect(Collectors.toList())))
                .collect(Collectors.toList());
        RaceWithEstimationData<ManeuverForEstimation> newRace = race.constructWithElements(competitorTracks);
        return newRace;
    }

}
