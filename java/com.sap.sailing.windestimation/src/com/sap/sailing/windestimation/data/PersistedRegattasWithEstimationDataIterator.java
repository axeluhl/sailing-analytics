package com.sap.sailing.windestimation.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedRegattasWithEstimationDataIterator<T> implements Iterator<RegattaWithEstimationData<T>> {

    private PersistedRacesWithEstimationDataIterator<T> racesIterator;
    private RaceWithEstimationData<T> nextRegatta;

    public PersistedRegattasWithEstimationDataIterator(EstimationDataPersistenceManager persistenceManager) {
        racesIterator = new PersistedRacesWithEstimationDataIterator<T>(persistenceManager);
        nextRegatta = racesIterator.next();
    }

    @Override
    public boolean hasNext() {
        return nextRegatta != null;
    }

    @Override
    public RegattaWithEstimationData<T> next() {
        if (hasNext()) {
            List<RaceWithEstimationData<T>> racesList = new ArrayList<>();
            RaceWithEstimationData<T> race = nextRegatta;
            String regattaName = race.getRegattaName();
            racesList.add(race);
            while (racesIterator.hasNext()) {
                race = racesIterator.next();
                if (regattaName.equals(race.getRegattaName())) {
                    racesList.add(race);
                } else {
                    break;
                }
            }
            this.nextRegatta = racesIterator.hasNext() ? race : null;
        }
        return null;
    }

}
