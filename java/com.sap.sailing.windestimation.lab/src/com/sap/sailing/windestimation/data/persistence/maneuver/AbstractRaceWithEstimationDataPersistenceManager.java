package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCollection;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.RegattaWithEstimationData;
import com.sap.sailing.windestimation.data.WindQuality;
import com.sap.sailing.windestimation.data.serialization.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.RaceWithEstimationDataDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractRaceWithEstimationDataPersistenceManager<T> extends
        AbstractPersistenceManager<RaceWithEstimationData<T>> implements RaceWithEstimationDataPersistenceManager<T> {

    public static final String COMPETITOR_TRACKS_COLLECTION_NAME_EXTENSION = "competitorTracks";

    public AbstractRaceWithEstimationDataPersistenceManager() throws UnknownHostException {
    }

    @Override
    public void dropCollection() {
        getCollection().drop();
        getDb().getCollection(getCompetitorTracksCollectionName()).drop();
    }

    public abstract CompetitorTrackWithEstimationDataJsonDeserializer<T> getNewCompetitorTrackWithEstimationDataJsonDeserializer();

    @Override
    protected JsonDeserializer<RaceWithEstimationData<T>> getNewJsonDeserializer() {
        return new RaceWithEstimationDataDeserializer<T>(getNewCompetitorTrackWithEstimationDataJsonDeserializer()) {
            @Override
            public RaceWithEstimationData<T> deserialize(JSONObject raceJson) throws JsonDeserializationException {
                JSONArray competitorTrackIdsJson = (JSONArray) raceJson
                        .get(RaceWithEstimationDataDeserializer.COMPETITOR_TRACKS);
                JSONArray competitorTracksJson = new JSONArray();
                MongoCollection<Document> competitorTracksCollection = getDb()
                        .getCollection(getCompetitorTracksCollectionName());
                for (Object idObject : competitorTrackIdsJson) {
                    Document dbCompetitorTrack = competitorTracksCollection
                            .find(new Document(FIELD_DB_ID, new ObjectId((String) idObject))).first();
                    try {
                        JSONObject competitorTrackJson = getJSONObject(dbCompetitorTrack);
                        competitorTracksJson.add(competitorTrackJson);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                raceJson.put(RaceWithEstimationDataDeserializer.COMPETITOR_TRACKS, competitorTracksJson);
                return super.deserialize(raceJson);
            }
        };
    }

    @Override
    public void addRace(String regattaName, String trackedRaceName, WindQuality windQuality,
            List<JSONObject> competitorTracks) {
        List<Document> dbCompetitorTracks = new ArrayList<>(competitorTracks.size());
        for (JSONObject competitorTrack : competitorTracks) {
            Document entry = parseJsonString(competitorTrack.toString());
            dbCompetitorTracks.add(entry);
        }
        Document dbObject = new Document();
        dbObject.put(RaceWithEstimationDataDeserializer.REGATTA_NAME, regattaName);
        dbObject.put(RaceWithEstimationDataDeserializer.TRACKED_RACE_NAME, trackedRaceName);
        dbObject.put(RaceWithEstimationDataDeserializer.WIND_QUALITY, windQuality.name());
        MongoCollection<Document> competitorTracksCollection = getDb()
                .getCollection(getCompetitorTracksCollectionName());
        competitorTracksCollection.insertMany(dbCompetitorTracks);
        BasicDBList dbCompetitorTrackIds = new BasicDBList();
        for (Document dbCompetitorTrack : dbCompetitorTracks) {
            ObjectId dbId = (ObjectId) dbCompetitorTrack.get(FIELD_DB_ID);
            dbCompetitorTrackIds.add(dbId.toHexString());
        }
        dbObject.put(RaceWithEstimationDataDeserializer.COMPETITOR_TRACKS, dbCompetitorTrackIds);
        MongoCollection<Document> races = getDb().getCollection(getCollectionName());
        races.insertOne(dbObject);
    }

    protected String getCompetitorTracksCollectionName() {
        return getCollectionName() + "." + COMPETITOR_TRACKS_COLLECTION_NAME_EXTENSION;
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