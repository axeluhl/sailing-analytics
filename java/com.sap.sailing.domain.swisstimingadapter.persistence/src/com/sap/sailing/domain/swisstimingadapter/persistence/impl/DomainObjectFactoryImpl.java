package com.sap.sailing.domain.swisstimingadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB database;
    
    private final SwissTimingFactory swissTimingFactory;
    
    public DomainObjectFactoryImpl(DB db, SwissTimingFactory swissTimingFactory) {
        super();
        this.database = db;
        this.swissTimingFactory = swissTimingFactory;
        DBCollection rawMessages = database.getCollection(CollectionNames.RAW_MESSAGES.name());
        rawMessages.ensureIndex(new BasicDBObject().append(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), 1));
    }

    @Override
    public Iterable<SwissTimingConfiguration> getSwissTimingConfigurations() {
        List<SwissTimingConfiguration> result = new ArrayList<SwissTimingConfiguration>();
        try {
            DBCollection stConfigs = database.getCollection(CollectionNames.SWISSTIMING_CONFIGURATIONS.name());
            for (DBObject o : stConfigs.find()) {
                SwissTimingConfiguration stConfig = loadSwissTimingConfiguration(o);
                result.add(stConfig);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded TracTrac configurations. Check MongoDB settings.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "getTracTracConfigurations", t);
        }
        return result;
    }

    private SwissTimingConfiguration loadSwissTimingConfiguration(DBObject object) {
        return swissTimingFactory.createSwissTimingConfiguration((String) object.get(FieldNames.ST_CONFIG_NAME.name()),
                (String) object.get(FieldNames.ST_CONFIG_HOSTNAME.name()),
                (Integer) object.get(FieldNames.ST_CONFIG_PORT.name()));
    }

    @Override
    public List<SailMasterMessage> loadMessages(int firstSequenceNumber) {
        DBCollection rawMessages = database.getCollection(CollectionNames.RAW_MESSAGES.name());
        BasicDBObject query = new BasicDBObject();
        if (firstSequenceNumber != -1) {
            query.append(FieldNames.MESSAGE_SEQUENCE_NUMBER.name(), new BasicDBObject("$gte", firstSequenceNumber));
        }
        DBCursor results = rawMessages.find(query);
        List<SailMasterMessage> result = new ArrayList<SailMasterMessage>();
        for (DBObject o : results) {
            result.add(swissTimingFactory.createMessage((String) o.get(FieldNames.MESSAGE_CONTENT.name()),
                    (Long) o.get(FieldNames.MESSAGE_SEQUENCE_NUMBER.name())));
        }
        return result;
    }

}
