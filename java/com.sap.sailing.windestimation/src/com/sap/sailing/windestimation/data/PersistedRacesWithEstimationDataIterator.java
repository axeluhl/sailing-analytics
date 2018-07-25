package com.sap.sailing.windestimation.data;

import java.util.Iterator;

import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PersistedRacesWithEstimationDataIterator<T> implements Iterator<RaceWithEstimationData<T>> {

    private final EstimationDataPersistenceManager persistenceManager;
    private String lastDbId = null;
    private RaceWithEstimationData<T> nextRaceWithEstimationData = null;
    private final long numberOfRaces;
    private long currentRaceNumber = 0;
    private int numberOfCharsDuringLastStatusLog = 0;

    public PersistedRacesWithEstimationDataIterator(EstimationDataPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        numberOfRaces = persistenceManager.countRacesWithEstimationData();
        LoggingUtil.logInfo(numberOfRaces + " races found in MongoDB");
        prepareNext();
    }

    @Override
    public boolean hasNext() {
        return nextRaceWithEstimationData != null;
    }

    @Override
    public RaceWithEstimationData<T> next() {
        RaceWithEstimationData<T> nextRaceWithEstimationData = this.nextRaceWithEstimationData;
        prepareNext();
        return nextRaceWithEstimationData;
    }

    private void prepareNext() {
        LoggingUtil.delete(numberOfCharsDuringLastStatusLog);
        long nextRaceNumber = currentRaceNumber + 1;
        numberOfCharsDuringLastStatusLog = LoggingUtil.logInfo("Loading race " + nextRaceNumber + "/" + numberOfRaces
                + " (" + (nextRaceNumber * 100 / numberOfRaces) + " %)");
        try {
            nextRaceWithEstimationData = (RaceWithEstimationData<T>) persistenceManager.getNextRaceWithEstimationData(lastDbId);
            if (nextRaceWithEstimationData != null) {
                this.lastDbId = nextRaceWithEstimationData.getDbId();
                this.currentRaceNumber = nextRaceNumber;
            }
        } catch (JsonDeserializationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    public long getNumberOfRaces() {
        return numberOfRaces;
    }

}
