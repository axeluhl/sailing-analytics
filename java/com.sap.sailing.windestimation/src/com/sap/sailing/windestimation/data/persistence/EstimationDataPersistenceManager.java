package com.sap.sailing.windestimation.data.persistence;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 */
public interface EstimationDataPersistenceManager<T> {

    void dropDb();

    void addRace(String regattaName, String trackedRaceName, List<JSONObject> competitorTracks);

    long countRacesWithEstimationData();

    RaceWithEstimationData<T> getNextRaceWithEstimationData(String lastId)
            throws JsonDeserializationException, ParseException;

}
