package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final DB database;
    
    public MongoObjectFactoryImpl(DB database) {
        super();
        this.database = database;
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
