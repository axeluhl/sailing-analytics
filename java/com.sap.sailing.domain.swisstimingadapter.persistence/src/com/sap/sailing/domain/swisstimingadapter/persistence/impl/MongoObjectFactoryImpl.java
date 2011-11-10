package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.MessageType;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final DB database;
    
    public MongoObjectFactoryImpl(DB database) {
        super();
        this.database = database;
        
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
        result.put(FieldNames.ST_CONFIG_HOSTNAME.name(), swissTimingConfiguration.getHostname());
        result.put(FieldNames.ST_CONFIG_PORT.name(), swissTimingConfiguration.getPort());
        stConfigCollection.insert(result);
    }

    @Override
    public void storeRawSailMasterMessage(SailMasterMessage message) {
        DBCollection rawMessageCollection = database.getCollection(CollectionNames.RAW_MESSAGES.name());
        rawMessageCollection.insert(new BasicDBObject().append(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), message.getSequenceNumber()).
                append(FieldNames.MESSAGE_CONTENT.name(), message.getMessage()));
    }

    @Override
    public void storeSailMasterMessage(SailMasterMessage message) {
        
        DBCollection messageCollection = null;
        MessageType type = message.getType();

        BasicDBObject objToInsert = new BasicDBObject();

        objToInsert.put(FieldNames.MESSAGE_COMMAND.name(), message.getType().name());
        objToInsert.put(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), message.getSequenceNumber());
        objToInsert.put(FieldNames.MESSAGE_CONTENT.name(), message.getMessage());
        
        if(type.isRaceSpecific()) {
        	objToInsert.put(FieldNames.RACE_ID.name(), message.getRaceID());
            messageCollection = database.getCollection(CollectionNames.RACES_MESSAGES.name());
        }
        else {
            messageCollection = database.getCollection(CollectionNames.COMMAND_MESSAGES.name());
        }

        messageCollection.insert(objToInsert);
    }

    @Override
    public void storeRace(Race race) {
        DBCollection racesCollection = database.getCollection(CollectionNames.RACES_MASTERDATA.name());
     
        BasicDBObject query = new BasicDBObject();
        query.append(FieldNames.RACE_ID.name(), race.getRaceID());
        
        BasicDBObject result = new BasicDBObject();

        result.put(FieldNames.RACE_ID.name(), race.getRaceID());
        result.put(FieldNames.RACE_DESCRIPTION.name(), race.getDescription());
        result.put(FieldNames.RACE_STARTTIME.name(), new Long(race.getStartTime().asMillis()));

        racesCollection.update(query,result);
    }
    
}
