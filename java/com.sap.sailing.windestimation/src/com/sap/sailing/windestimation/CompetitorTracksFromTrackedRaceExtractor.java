package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

public interface CompetitorTracksFromTrackedRaceExtractor<T> {
    
    List<CompetitorTrackWithEstimationData<T>> extractCompetitorTracks(TrackedRace trackedRace, PolarDataService polarDataService);

}
