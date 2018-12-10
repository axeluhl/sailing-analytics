package com.sap.sse.mongodb.internal;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sap.sse.mongodb.AlreadyRegisteredException;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public class MongoDBServiceImpl implements MongoDBService {

    private static final Logger logger = Logger.getLogger(MongoDBServiceImpl.class.getName());

    private MongoDBConfiguration configuration;

    private final Map<MongoClientURI, MongoClient> mongos;
    
    private final Map<MongoClientURI, DB> dbs;

    /**
     * collection name -> fully qualified class name
     */
    private final Map<String, String> registered;

    public MongoDBServiceImpl() {
        mongos = new HashMap<>();
        dbs = new HashMap<>();
        registered = new HashMap<String, String>();
    }

    public MongoDBServiceImpl(MongoDBConfiguration configuration) {
        this();
        setConfiguration(configuration);
    }

    public MongoDBConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MongoDBConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Used Mongo configuration: "+configuration.getMongoClientURI());
    }

    public DB getDB() {
        if (configuration == null) {
            configuration = MongoDBConfiguration.getDefaultTestConfiguration();
            logger.info("Used default Mongo configuration: "+configuration.getMongoClientURI());
        }
        try {
            return getDB(configuration);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    private synchronized DB getDB(MongoDBConfiguration mongoDBConfiguration) throws UnknownHostException {
        final MongoClientURI key = mongoDBConfiguration.getMongoClientURI();
        DB db = dbs.get(key);
        if (db == null) {
            MongoClient mongo = mongos.get(key);
            if (mongo == null) {
                mongo = new MongoClient(mongoDBConfiguration.getMongoClientURI());
                mongos.put(key, mongo);
            }
            db = mongo.getDB(mongoDBConfiguration.getMongoClientURI().getDatabase());
            dbs.put(key, db);
        }
        return db;
    }

    @Override
    public void registerExclusively(Class<?> registerForInterface, String collectionName)
            throws AlreadyRegisteredException {
        String fullyQualified = registerForInterface.getName();
        if (registered.keySet().contains(collectionName) && registered.get(collectionName) != fullyQualified) {
            logger.log(Level.SEVERE, "Same collection name (" + collectionName
                    + " is required in two different places - this may lead to problems: \n"
                    + " - already registered for: " + registered.get(collectionName) + "\n"
                    + " - tried to register for: " + fullyQualified);
            throw new AlreadyRegisteredException();
        }
        logger.log(Level.INFO, "Registered collection name: " + collectionName);
        registered.put(collectionName, fullyQualified);
    }
}
