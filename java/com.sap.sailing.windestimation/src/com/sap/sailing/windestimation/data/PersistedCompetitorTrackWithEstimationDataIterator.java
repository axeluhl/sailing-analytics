package com.sap.sailing.windestimation.data;

import java.util.Iterator;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedCompetitorTrackWithEstimationDataIterator implements Iterator<CompetitorTrackWithEstimationData> {

    private final PersistedRacesWithEstimationDataIterator racesIterator;
    private Iterator<CompetitorTrackWithEstimationData> competitorTracksIteratorOfCurrentRace = null;

    public PersistedCompetitorTrackWithEstimationDataIterator(EstimationDataPersistenceManager persistenceManager) {
        racesIterator = new PersistedRacesWithEstimationDataIterator(persistenceManager);
    }

    @Override
    public boolean hasNext() {
        return competitorTracksIteratorOfCurrentRace != null && competitorTracksIteratorOfCurrentRace.hasNext()
                || racesIterator.hasNext();
    }

    @Override
    public CompetitorTrackWithEstimationData next() {
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
