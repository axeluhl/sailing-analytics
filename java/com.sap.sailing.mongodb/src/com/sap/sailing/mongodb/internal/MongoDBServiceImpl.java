package com.sap.sailing.mongodb.internal;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.mongodb.MongoDBService;

public class MongoDBServiceImpl implements MongoDBService {

    private static final Logger logger = Logger.getLogger(MongoDBServiceImpl.class.getName());

    private MongoDBConfiguration configuration;

    private final Map<Util.Pair<String, Integer>, Mongo> mongos;
    
    private final Map<Util.Pair<String, Integer>, DB> dbs;
    
    public MongoDBServiceImpl() {
        mongos = new HashMap<Util.Pair<String, Integer>, Mongo>();
        dbs = new HashMap<Util.Pair<String,Integer>, DB>();
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
        logger.info("Used Mongo configuration: host:port/DBName: "+configuration.getHostName()+":"+configuration.getPort()+"/"+configuration.getDatabaseName());
    }

    public DB getDB() {
        if (configuration == null) {
            configuration = MongoDBConfiguration.getDefaultTestConfiguration();
            logger.info("Used default Mongo configuration: host:port/DBName: "+configuration.getHostName()+":"+configuration.getPort()+"/"+configuration.getDatabaseName());
        }
        // check if mongodb is disabled by configuring host="none", e.g. for strategy simulation
        if (configuration.getHostName().equals("none")) {
            return null; // if mongodb is disabled, getDB() allways return null
        } else {
            try {
                return getDB(configuration);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private synchronized DB getDB(MongoDBConfiguration mongoDBConfiguration) throws UnknownHostException {
        Util.Pair<String, Integer> key = new Util.Pair<String, Integer>(mongoDBConfiguration.getHostName(), mongoDBConfiguration.getPort());
        DB db = dbs.get(key);
        if (db == null) {
            Mongo mongo = mongos.get(key);
            if (mongo == null) {
                mongo = new Mongo(mongoDBConfiguration.getHostName(), mongoDBConfiguration.getPort());
                mongos.put(key, mongo);
            }
            db = mongo.getDB(mongoDBConfiguration.getDatabaseName());
            dbs.put(key, db);
        }
        return db;
    }
}
