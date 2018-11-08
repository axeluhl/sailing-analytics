package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

public class CompetitorTrackFilteringImpl<T> implements DataFilteringPredicate<CompetitorTrackWithEstimationData<T>> {

    @Override
    public boolean test(CompetitorTrackWithEstimationData<T> competitorTrack) {
        return competitorTrack.isClean();
    }

}
