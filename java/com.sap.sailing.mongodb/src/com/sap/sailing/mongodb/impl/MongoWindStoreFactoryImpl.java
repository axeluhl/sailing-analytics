package com.sap.sailing.mongodb.impl;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.mongodb.DomainObjectFactory;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoWindStore;
import com.sap.sailing.mongodb.MongoWindStoreFactory;
import com.sap.sailing.util.Util.Pair;

public class MongoWindStoreFactoryImpl implements MongoWindStoreFactory, BundleActivator {
    private static final String MONGO_PORT = "mongo.port";

    private static final String MONGO_HOSTNAME = "mongo.host";

    private static final String MONGO_DB_NAME = "mongo.dbName";

    private static final Logger logger = Logger.getLogger(MongoWindStoreFactoryImpl.class.getName());
    
    private static MongoWindStoreFactory defaultInstance;
    
    private String defaultHostName;
    private int defaultPort;
    private String defaultDatabaseName;
    private final Map<Pair<String, Integer>, Mongo> mongos;
    
    public MongoWindStoreFactoryImpl() {
        defaultHostName = System.getProperty(MONGO_HOSTNAME, "127.0.0.1");
        defaultPort = Integer.valueOf(System.getProperty(MONGO_PORT, "27017"));
        defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_DB_NAME);
        mongos = new HashMap<Pair<String, Integer>, Mongo>();
    }
    
    @Override
    public MongoWindStore getMongoWindStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) throws UnknownHostException, MongoException {
        return getMongoWindStore(defaultHostName, defaultPort, defaultDatabaseName, mongoObjectFactory, domainObjectFactory);
    }

    private MongoWindStore getMongoWindStore(String hostname, int port, String dbName,
            MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) throws UnknownHostException, MongoException {
        return new MongoWindStoreImpl(getDB(hostname, port, dbName), mongoObjectFactory, domainObjectFactory);
    }

    @Override
    public DB getDB() {
        try {
            return getDB(defaultHostName, defaultPort, defaultDatabaseName);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    private synchronized DB getDB(String hostname, int port, String dbName) throws UnknownHostException {
        Pair<String, Integer> key = new Pair<String, Integer>(hostname, port);
        Mongo mongo = mongos.get(key);
        if (mongo == null) {
            mongo = new Mongo(hostname, port);
            mongos.put(key, mongo);
        }
        return mongo.getDB(dbName);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        defaultInstance = this;
        defaultDatabaseName = context.getProperty(MONGO_DB_NAME);
        if (defaultDatabaseName == null) {
            defaultDatabaseName = DEFAULT_DB_NAME;
        } else {
            logger.log(Level.INFO, "found mongo.dbName="+defaultDatabaseName);
        }
        defaultHostName = context.getProperty(MONGO_HOSTNAME);
        if (defaultHostName == null) {
            defaultHostName = "127.0.0.1";
        } else {
            logger.log(Level.INFO, "found "+MONGO_HOSTNAME+"="+defaultHostName);
        }
        defaultPort = context.getProperty(MONGO_PORT) != null ? Integer.valueOf(context.getProperty(MONGO_PORT)) : 27017;
        logger.log(Level.INFO, "Using port "+defaultPort+" as default for Mongo wind stores");
    }
    
    public static MongoWindStoreFactory getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new MongoWindStoreFactoryImpl();
        }
        return defaultInstance;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
