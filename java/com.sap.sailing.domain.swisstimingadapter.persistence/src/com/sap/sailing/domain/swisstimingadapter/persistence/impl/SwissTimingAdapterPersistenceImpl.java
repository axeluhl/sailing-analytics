package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.mongodb.MongoDBService;

public class SwissTimingAdapterPersistenceImpl implements SwissTimingAdapterPersistence {

    private final DB database;

    private final SwissTimingFactory swissTimingFactory;

    private static final Logger logger = Logger.getLogger(SwissTimingAdapterPersistenceImpl.class.getName());
    
    private final DBCollection lastMessageCountCollection;

    /**
     * this race cache should only be used for checks in the storeSailMasterMessage method to ensure that we have always
     * a valid race for a message (also in case we missed a RAC message)
     */
    private HashMap<String, Race> cachedRaces = new HashMap<String, Race>();

    private long lastMessageCount;

    public SwissTimingAdapterPersistenceImpl(MongoDBService mongoDBService, SwissTimingFactory swissTimingFactory) {
        super();
        this.database = mongoDBService.getDB();
        this.swissTimingFactory = swissTimingFactory;
        lastMessageCountCollection = database.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        init();
    }

    private void init() {
        // ensure the required indexes for the collection of race specific messages
        DBCollection racesMessageCollection = database.getCollection(CollectionNames.RACES_MESSAGES.name());

        BasicDBObject indexKeysRaceMsgs = new BasicDBObject();
        indexKeysRaceMsgs.put(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), 1);
        indexKeysRaceMsgs.put(FieldNames.RACE_ID.name(), 1);

        racesMessageCollection.ensureIndex(indexKeysRaceMsgs, IndexNames.INDEX_RACES_MESSAGES.name(), true);

        // ensure the required indexes for the collection of command messages
        DBCollection cmdMessagesCollection = database.getCollection(CollectionNames.COMMAND_MESSAGES.name());

        BasicDBObject indexKeysCmdMsgs = new BasicDBObject();
        indexKeysCmdMsgs.put(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), 1);

        cmdMessagesCollection.ensureIndex(indexKeysCmdMsgs, IndexNames.INDEX_COMMAND_MESSAGES.name(), true);

        // initially fill the races cache
        Iterable<Race> races = getRaces();
        for (Race race : races) {
            cachedRaces.put(race.getRaceID(), race);
        }
    }

    @Override
    public Iterable<SwissTimingConfiguration> getSwissTimingConfigurations() {
        List<SwissTimingConfiguration> result = new ArrayList<SwissTimingConfiguration>();
        try {
            DBCollection stConfigs = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
            for (DBObject o : stConfigs.find()) {
                SwissTimingConfiguration stConfig = loadSwissTimingConfiguration(o);
                // the old swisstiming config was not based on a json URL -> ignore such configs
                if(stConfig.getJsonURL() != null) {
                    result.add(stConfig);
                }
            }
            Collections.reverse(result);
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE,
                    "Error connecting to MongoDB, unable to load recorded SwissTiming configurations. Check MongoDB settings.");
            logger.throwing(SwissTimingAdapterPersistenceImpl.class.getName(), "getSwissTimingConfigurations", e);
        }
        return result;
    }

    private SwissTimingConfiguration loadSwissTimingConfiguration(DBObject object) {
        String name = (String) object.get(FieldNames.ST_CONFIG_NAME.name());
        String jsonURL = (String) object.get(FieldNames.ST_CONFIG_JSON_URL.name());
        String hostname = (String) object.get(FieldNames.ST_CONFIG_HOSTNAME.name());
        Integer port = (Integer) object.get(FieldNames.ST_CONFIG_PORT.name());

        return swissTimingFactory.createSwissTimingConfiguration(name, jsonURL, hostname, port);
    }

    @Override
    public Iterable<SwissTimingArchiveConfiguration> getSwissTimingArchiveConfigurations() {
        List<SwissTimingArchiveConfiguration> result = new ArrayList<SwissTimingArchiveConfiguration>();
        try {
            DBCollection stConfigs = database.getCollection(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
            for (DBObject o : stConfigs.find()) {
                SwissTimingArchiveConfiguration stConfig = loadSwissTimingArchiveConfiguration(o);
                result.add(stConfig);
            }
            Collections.reverse(result);
        } catch (Exception e) {
            // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE,
                    "Error connecting to MongoDB, unable to load recorded SwissTiming archive configurations. Check MongoDB settings.");
            logger.throwing(SwissTimingAdapterPersistenceImpl.class.getName(), "getSwissTimingArchiveConfigurations", e);
        }
        return result;
    }

    private SwissTimingArchiveConfiguration loadSwissTimingArchiveConfiguration(DBObject object) {
        return swissTimingFactory.createSwissTimingArchiveConfiguration((String) object.get(FieldNames.ST_ARCHIVE_JSON_URL.name()));
    }

    @Override
    public List<SailMasterMessage> loadCommandMessages(int firstSequenceNumber) {
        DBCollection commandMessages = database.getCollection(CollectionNames.COMMAND_MESSAGES.name());
        BasicDBObject query = new BasicDBObject();
        if (firstSequenceNumber != -1) {
            query.append(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), new BasicDBObject("$gte", firstSequenceNumber));
        }
        DBCursor results = commandMessages.find(query);
        List<SailMasterMessage> result = new ArrayList<SailMasterMessage>();
        for (DBObject o : results) {
            result.add(swissTimingFactory.createMessage((String) o.get(FieldNames.MESSAGE_CONTENT.name())));
        }
        return result;
    }

    @Override
    public List<SailMasterMessage> loadRaceMessages(String raceID) {
        DBCollection racesMessagesCollection = database.getCollection(CollectionNames.RACES_MESSAGES.name());
        racesMessagesCollection.ensureIndex(new BasicDBObject(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), 1)); // no sort without index
        BasicDBObject query = new BasicDBObject();
        query.append(FieldNames.RACE_ID.name(), raceID);
        DBCursor results = racesMessagesCollection.find(query).sort(
                new BasicDBObject().append(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), 1));
        List<SailMasterMessage> result = new ArrayList<SailMasterMessage>();
        for (DBObject o : results) {
            SailMasterMessage msg = swissTimingFactory.createMessage((String) o.get(FieldNames.MESSAGE_CONTENT.name()));
            result.add(msg);
        }
        return result;
    }

    @Override
    public List<SailMasterMessage> loadCommandMessages() {
        DBCollection cmdMessagesCollection = database.getCollection(CollectionNames.COMMAND_MESSAGES.name());
        DBCursor results = cmdMessagesCollection.find().sort(
                new BasicDBObject().append(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), 1));
        List<SailMasterMessage> result = new ArrayList<SailMasterMessage>();
        for (DBObject o : results) {
            SailMasterMessage msg = swissTimingFactory.createMessage((String) o.get(FieldNames.MESSAGE_CONTENT.name()));
            result.add(msg);
        }
        return result;
    }

    @Override
    public Race getRace(String raceID) {
        DBCollection races = database.getCollection(CollectionNames.RACES_MASTERDATA.name());
        BasicDBObject query = new BasicDBObject();
        query.append(FieldNames.RACE_ID.name(), raceID);
        DBObject o = races.findOne(query);
        if (o != null) {
            Long startTimeAsMillis = (Long) o.get(FieldNames.RACE_STARTTIME.name());
            Race race = swissTimingFactory.createRace((String) o.get(FieldNames.RACE_ID.name()), (String) o
                    .get(FieldNames.RACE_DESCRIPTION.name()), startTimeAsMillis == null ? null
                    : new MillisecondsTimePoint(startTimeAsMillis));
            return race;
        }
        return null;
    }

    @Override
    public boolean hasRaceStartlist(String raceID) {
        DBCollection races = database.getCollection(CollectionNames.RACES_MESSAGES.name());
        BasicDBObject query = new BasicDBObject();
        query.append(FieldNames.RACE_ID.name(), raceID);
        query.append(FieldNames.MESSAGE_COMMAND.name(), MessageType.STL.name());
        DBObject object = races.findOne(query);
        return object != null; 
    }

    @Override
    public boolean hasRaceCourse(String raceID) {
        DBCollection races = database.getCollection(CollectionNames.RACES_MESSAGES.name());
        BasicDBObject query = new BasicDBObject();
        query.append(FieldNames.RACE_ID.name(), raceID);
        query.append(FieldNames.MESSAGE_COMMAND.name(), MessageType.CCG.name());
        DBObject object = races.findOne(query);
        return object != null; 
    }
    
    @Override
    public Iterable<Race> getRaces() {
        DBCollection races = database.getCollection(CollectionNames.RACES_MASTERDATA.name());
        DBCursor results = races.find();
        List<Race> result = new ArrayList<Race>();
        for (DBObject o : results) {
            Long startTimeAsMillis = (Long) o.get(FieldNames.RACE_STARTTIME.name());
            Race race = swissTimingFactory.createRace((String) o.get(FieldNames.RACE_ID.name()), (String) o
                    .get(FieldNames.RACE_DESCRIPTION.name()), startTimeAsMillis == null ? null
                    : new MillisecondsTimePoint(startTimeAsMillis));
            result.add(race);
        }
        return result;
    }

    @Override
    public void storeSwissTimingConfiguration(SwissTimingConfiguration swissTimingConfiguration) {
        DBCollection stConfigCollection = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
        stConfigCollection.ensureIndex(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.ST_CONFIG_NAME.name(), swissTimingConfiguration.getName());
        for (DBObject equallyNamedConfig : stConfigCollection.find(result)) {
            stConfigCollection.remove(equallyNamedConfig);
        }
        result.put(FieldNames.ST_CONFIG_JSON_URL.name(), swissTimingConfiguration.getJsonURL());
        result.put(FieldNames.ST_CONFIG_HOSTNAME.name(), swissTimingConfiguration.getHostname());
        result.put(FieldNames.ST_CONFIG_PORT.name(), swissTimingConfiguration.getPort());

        stConfigCollection.insert(result);
    }

    @Override
    public void storeSwissTimingArchiveConfiguration(
            SwissTimingArchiveConfiguration createSwissTimingArchiveConfiguration) {
        DBCollection stArchiveConfigCollection = database.getCollection(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
        stArchiveConfigCollection.ensureIndex(CollectionNames.SWISSTIMING_ARCHIVE_CONFIGURATIONS.name());
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.ST_ARCHIVE_JSON_URL.name(), createSwissTimingArchiveConfiguration.getJsonUrl());
        for (DBObject equallyNamedConfig : stArchiveConfigCollection.find(result)) {
            stArchiveConfigCollection.remove(equallyNamedConfig);
        }
        stArchiveConfigCollection.insert(result);
    }

    @Override
    public void storeSailMasterMessage(SailMasterMessage message) {
        // Attention: this method is very time critical as we will receive thousands of messages in a short time
        DBCollection messageCollection = null;
        MessageType type = message.getType();
        BasicDBObject objToInsert = new BasicDBObject();
        objToInsert.put(FieldNames.MESSAGE_COMMAND.name(), message.getType().name());
        objToInsert.put(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), message.getSequenceNumber());
        objToInsert.put(FieldNames.MESSAGE_CONTENT.name(), message.getMessage());
        if(type.isRaceSpecific()) {
                objToInsert.put(FieldNames.RACE_ID.name(), message.getRaceID());
            messageCollection = database.getCollection(CollectionNames.RACES_MESSAGES.name());
            if (message.getSequenceNumber() == null) {
                DBObject emptyQuery = new BasicDBObject();
                DBObject incrementLastMessageCountQuery = new BasicDBObject().
                        append("$inc", new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 1));
                DBObject newCountRecord = lastMessageCountCollection.findAndModify(emptyQuery, incrementLastMessageCountQuery);
                lastMessageCount = (Long) newCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name());
                objToInsert.put(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), lastMessageCount);
            }
        } else {
            messageCollection = database.getCollection(CollectionNames.COMMAND_MESSAGES.name());
        }
        messageCollection.insert(objToInsert);
        if(message.getType() == MessageType.RAC) {
            // store the new race in the master data collection
            List<Race> availableRaces = parseAvailableRacesMessage(message);
            for (Race newRace : availableRaces) {
                storeRace(newRace);
            }
        } else if (message.getRaceID() != null && !cachedRaces.containsKey(message.getRaceID())) {
            // ah, we found a new raceID which is not in the list of known races
            // in order to have a more intelligent conflict resolver mechanism we will forward the resolution to a special thread later on
            boolean simpleResolution = true;
            if (simpleResolution) {
                // first check if the missing race has been created in the mean time
                Race raceFromDB = getRace(message.getRaceID());
                if (raceFromDB != null) {
                    cachedRaces.put(raceFromDB.getRaceID(), raceFromDB);
                } else {
                    logger.info("Didn't find race "+message.getRaceID()+" in race DB. Adding it.");
                    Race newRace = SwissTimingFactory.INSTANCE.createRace(message.getRaceID(), null, null);
                    storeRace(newRace);
                    cachedRaces.put(newRace.getRaceID(), newRace);
                }
            }
        }
    }

    private List<Race> parseAvailableRacesMessage(SailMasterMessage availableRacesMessage) {
        int count = Integer.valueOf(availableRacesMessage.getSections()[1]);
        List<Race> result = new ArrayList<Race>();
        for (int i = 0; i < count; i++) {
            String[] idAndDescription = availableRacesMessage.getSections()[2 + i].split(";");
            result.add(SwissTimingFactory.INSTANCE.createRace(idAndDescription[0], idAndDescription[1], null));
        }
        return result;
    }

    @Override
    public void storeRace(Race race) {
        DBCollection racesCollection = database.getCollection(CollectionNames.RACES_MASTERDATA.name());
        BasicDBObject query = new BasicDBObject();
        query.append(FieldNames.RACE_ID.name(), race.getRaceID());
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.RACE_ID.name(), race.getRaceID());
        result.put(FieldNames.RACE_DESCRIPTION.name(), race.getDescription());
        result.put(FieldNames.RACE_STARTTIME.name(), race.getStartTime() == null ? null : new Long(race.getStartTime()
                .asMillis()));
        racesCollection.update(query, result, /* upsrt */true, /* multi */false);
    }

    @Override
    public void dropAllRaceMasterData() {
        DBCollection racesCollection = database.getCollection(CollectionNames.RACES_MASTERDATA.name());
        racesCollection.drop();
    }

    @Override
    public void dropAllMessageData() {
        DBCollection racesMessageCollection = database.getCollection(CollectionNames.RACES_MESSAGES.name());
        racesMessageCollection.drop();
        DBCollection cmdMessageCollection = database.getCollection(CollectionNames.COMMAND_MESSAGES.name());
        cmdMessageCollection.drop();
        DBCollection lastMessageCountCollection = database.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCountCollection.drop();
        database.getLastError(); // wait for the drop() to complete
    }
}
