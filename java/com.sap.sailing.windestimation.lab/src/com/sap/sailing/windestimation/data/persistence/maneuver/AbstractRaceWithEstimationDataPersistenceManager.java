package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.RegattaWithEstimationData;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.RaceWithEstimationDataDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractRaceWithEstimationDataPersistenceManager<T> extends
        AbstractPersistenceManager<RaceWithEstimationData<T>> implements RaceWithEstimationDataPersistenceManager<T> {

    public AbstractRaceWithEstimationDataPersistenceManager() throws UnknownHostException {
    }

    @Override
    public void dropDb() {
        getDb().getCollection(getCollectionName()).drop();
    }

    public abstract CompetitorTrackWithEstimationDataJsonDeserializer<T> getNewCompetitorTrackWithEstimationDataJsonDeserializer();

    @Override
    protected JsonDeserializer<RaceWithEstimationData<T>> getNewJsonDeserializer() {
        return new RaceWithEstimationDataDeserializer<>(getNewCompetitorTrackWithEstimationDataJsonDeserializer());
    }

    public void addRace(String regattaName, String trackedRaceName, List<JSONObject> competitorTracks) {
        BasicDBList dbCompetitorTracks = new BasicDBList();
        for (JSONObject competitorTrack : competitorTracks) {
            DBObject entry = (DBObject) JSON.parse(competitorTrack.toString());
            dbCompetitorTracks.add(entry);
        }
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(RaceWithEstimationDataDeserializer.REGATTA_NAME, regattaName);
        dbObject.put(RaceWithEstimationDataDeserializer.TRACKED_RACE_NAME, trackedRaceName);
        dbObject.put(RaceWithEstimationDataDeserializer.COMPETITOR_TRACKS, dbCompetitorTracks);
        DBCollection races = getDb().getCollection(getCollectionName());
        races.insert(dbObject);
    }

    public Iterator<CompetitorTrackWithEstimationData<T>> getCompetitorTrackIterator(String query) {
        return new PersistedCompetitorTrackWithEstimationDataIteratorImpl(query);
    }

    public Iterator<T> getCompetitorTrackElementsIterator(String query) {
        return new PersistedCompetitorTrackElementsIteratorImpl(query);
    }

    public Iterator<RegattaWithEstimationData<T>> getRegattaIterator(String query) {
        return new PersistedRegattasWithEstimationDataIteratorImpl(query);
    }

    protected class PersistedCompetitorTrackWithEstimationDataIteratorImpl
            implements Iterator<CompetitorTrackWithEstimationData<T>> {

        private final PersistedElementsIterator<RaceWithEstimationData<T>> racesIterator;
        private Iterator<CompetitorTrackWithEstimationData<T>> competitorTracksIteratorOfCurrentRace = null;

        public PersistedCompetitorTrackWithEstimationDataIteratorImpl(String query) {
            racesIterator = getIterator(query);
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

    protected class PersistedCompetitorTrackElementsIteratorImpl implements Iterator<T> {

        private final Iterator<CompetitorTrackWithEstimationData<T>> competitorTrackIterator;
        private Iterator<T> elementsIteratorOfCurrentCompetitorTrack = null;

        public PersistedCompetitorTrackElementsIteratorImpl(String query) {
            this.competitorTrackIterator = getCompetitorTrackIterator(query);
        }

        @Override
        public boolean hasNext() {
            return elementsIteratorOfCurrentCompetitorTrack != null
                    && elementsIteratorOfCurrentCompetitorTrack.hasNext() || competitorTrackIterator.hasNext();
        }

        @Override
        public T next() {
            if (elementsIteratorOfCurrentCompetitorTrack != null
                    && elementsIteratorOfCurrentCompetitorTrack.hasNext()) {
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

    protected class PersistedRegattasWithEstimationDataIteratorImpl implements Iterator<RegattaWithEstimationData<T>> {

        private Iterator<RaceWithEstimationData<T>> racesIterator;
        private RaceWithEstimationData<T> nextRegatta;

        public PersistedRegattasWithEstimationDataIteratorImpl(String query) {
            racesIterator = getIterator(query);
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

}