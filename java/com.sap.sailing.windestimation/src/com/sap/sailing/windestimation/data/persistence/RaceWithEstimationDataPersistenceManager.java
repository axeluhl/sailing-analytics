package com.sap.sailing.windestimation.data.persistence;

import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.RegattaWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 */
public interface RaceWithEstimationDataPersistenceManager<T> extends PersistenceManager<RaceWithEstimationData<T>> {

    void dropDb();

    void addRace(String regattaName, String trackedRaceName, List<JSONObject> competitorTracks);

    Iterator<CompetitorTrackWithEstimationData<T>> getCompetitorTrackIterator(String query);

    Iterator<T> getCompetitorTrackElementsIterator(String query);

    Iterator<RegattaWithEstimationData<T>> getRegattaIterator(String query);

}
