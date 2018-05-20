package com.sap.sailing.windestimation.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedRegattasWithEstimationDataIterator implements Iterator<RegattaWithEstimationData> {

    private PersistedRacesWithEstimationDataIterator racesIterator;
    private RaceWithEstimationData nextRegatta;

    public PersistedRegattasWithEstimationDataIterator(EstimationDataPersistenceManager persistenceManager) {
        racesIterator = new PersistedRacesWithEstimationDataIterator(persistenceManager);
        nextRegatta = racesIterator.next();
    }

    @Override
    public boolean hasNext() {
        return nextRegatta != null;
    }

    @Override
    public RegattaWithEstimationData next() {
        if (hasNext()) {
            List<RaceWithEstimationData> racesList = new ArrayList<>();
            RaceWithEstimationData race = nextRegatta;
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
