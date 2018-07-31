package com.sap.sailing.windestimation.data;

import java.util.Iterator;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedElementsWithEstimationDataIterator<T> implements Iterator<T> {

    private final PersistedCompetitorTrackWithEstimationDataIterator<T> competitorTrackIterator;
    private Iterator<T> elementsIteratorOfCurrentCompetitorTrack = null;

    public PersistedElementsWithEstimationDataIterator(
            PersistedCompetitorTrackWithEstimationDataIterator<T> competitorTrackIterator) {
        this.competitorTrackIterator = competitorTrackIterator;
    }

    @Override
    public boolean hasNext() {
        return elementsIteratorOfCurrentCompetitorTrack != null && elementsIteratorOfCurrentCompetitorTrack.hasNext()
                || competitorTrackIterator.hasNext();
    }

    @Override
    public T next() {
        if (elementsIteratorOfCurrentCompetitorTrack != null && elementsIteratorOfCurrentCompetitorTrack.hasNext()) {
            return elementsIteratorOfCurrentCompetitorTrack.next();
        }
        while (competitorTrackIterator.hasNext()) {
            elementsIteratorOfCurrentCompetitorTrack = competitorTrackIterator.next().getElements().iterator();
            if (elementsIteratorOfCurrentCompetitorTrack.hasNext()) {
                return elementsIteratorOfCurrentCompetitorTrack.next();
            }
        }
        return null;
    }

}
