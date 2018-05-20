package com.sap.sailing.windestimation.data;

import java.util.Iterator;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedManeuverWithEstimationDataIterator implements Iterator<CompleteManeuverCurveWithEstimationData> {

    private final PersistedCompetitorTrackWithEstimationDataIterator competitorTrackIterator;
    private Iterator<CompleteManeuverCurveWithEstimationData> maneuversIteratorOfCurrentCompetitorTrack = null;

    public PersistedManeuverWithEstimationDataIterator(
            PersistedCompetitorTrackWithEstimationDataIterator competitorTrackIterator) {
        this.competitorTrackIterator = competitorTrackIterator;
    }

    @Override
    public boolean hasNext() {
        return maneuversIteratorOfCurrentCompetitorTrack != null && maneuversIteratorOfCurrentCompetitorTrack.hasNext()
                || competitorTrackIterator.hasNext();
    }

    @Override
    public CompleteManeuverCurveWithEstimationData next() {
        if (maneuversIteratorOfCurrentCompetitorTrack != null && maneuversIteratorOfCurrentCompetitorTrack.hasNext()) {
            return maneuversIteratorOfCurrentCompetitorTrack.next();
        }
        while (competitorTrackIterator.hasNext()) {
            maneuversIteratorOfCurrentCompetitorTrack = competitorTrackIterator.next().getManeuverCurves().iterator();
            if (maneuversIteratorOfCurrentCompetitorTrack.hasNext()) {
                return maneuversIteratorOfCurrentCompetitorTrack.next();
            }
        }
        return null;
    }

}
