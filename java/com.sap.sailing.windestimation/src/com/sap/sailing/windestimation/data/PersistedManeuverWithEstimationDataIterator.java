package com.sap.sailing.windestimation.data;

import java.util.Iterator;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedManeuverWithEstimationDataIterator<T> implements Iterator<T> {

    private final PersistedCompetitorTrackWithEstimationDataIterator<T> competitorTrackIterator;
    private Iterator<T> maneuversIteratorOfCurrentCompetitorTrack = null;

    public PersistedManeuverWithEstimationDataIterator(
            PersistedCompetitorTrackWithEstimationDataIterator<T> competitorTrackIterator) {
        this.competitorTrackIterator = competitorTrackIterator;
    }

    @Override
    public boolean hasNext() {
        return maneuversIteratorOfCurrentCompetitorTrack != null && maneuversIteratorOfCurrentCompetitorTrack.hasNext()
                || competitorTrackIterator.hasNext();
    }

    @Override
    public T next() {
        if (maneuversIteratorOfCurrentCompetitorTrack != null && maneuversIteratorOfCurrentCompetitorTrack.hasNext()) {
            return maneuversIteratorOfCurrentCompetitorTrack.next();
        }
        while (competitorTrackIterator.hasNext()) {
            maneuversIteratorOfCurrentCompetitorTrack = competitorTrackIterator.next().getElements().iterator();
            if (maneuversIteratorOfCurrentCompetitorTrack.hasNext()) {
                return maneuversIteratorOfCurrentCompetitorTrack.next();
            }
        }
        return null;
    }

}
