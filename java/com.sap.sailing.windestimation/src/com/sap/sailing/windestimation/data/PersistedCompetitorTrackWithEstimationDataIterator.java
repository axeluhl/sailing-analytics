package com.sap.sailing.windestimation.data;

import java.util.Iterator;

import com.sap.sailing.windestimation.data.persistence.EstimationDataPersistenceManager;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedCompetitorTrackWithEstimationDataIterator<T>
        implements Iterator<CompetitorTrackWithEstimationData<T>> {

    private final PersistedRacesWithEstimationDataIterator<T> racesIterator;
    private Iterator<CompetitorTrackWithEstimationData<T>> competitorTracksIteratorOfCurrentRace = null;

    public PersistedCompetitorTrackWithEstimationDataIterator(EstimationDataPersistenceManager<T> persistenceManager) {
        racesIterator = new PersistedRacesWithEstimationDataIterator<>(persistenceManager);
    }

    @Override
    public boolean hasNext() {
        return competitorTracksIteratorOfCurrentRace != null && competitorTracksIteratorOfCurrentRace.hasNext()
                || racesIterator.hasNext();
    }

    @Override
    public CompetitorTrackWithEstimationData<T> next() {
        if (competitorTracksIteratorOfCurrentRace != null && competitorTracksIteratorOfCurrentRace.hasNext()) {
            return competitorTracksIteratorOfCurrentRace.next();
        }
        while (racesIterator.hasNext()) {
            competitorTracksIteratorOfCurrentRace = racesIterator.next().getCompetitorTracks().iterator();
            if (competitorTracksIteratorOfCurrentRace.hasNext()) {
                return competitorTracksIteratorOfCurrentRace.next();
            }
        }
        return null;
    }

}
