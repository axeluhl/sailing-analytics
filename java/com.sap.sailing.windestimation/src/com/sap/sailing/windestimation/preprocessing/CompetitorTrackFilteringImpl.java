package com.sap.sailing.windestimation.preprocessing;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

/**
 * Predicate to test whether a competitor track represents a competitor who completely a race normally without standing
 * still, or being sunk during the race.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompetitorTrackFilteringImpl implements DataFilteringPredicate<CompetitorTrackWithEstimationData<?>> {

    @Override
    public boolean test(CompetitorTrackWithEstimationData<?> competitorTrack) {
        return competitorTrack.isClean();
    }

}
