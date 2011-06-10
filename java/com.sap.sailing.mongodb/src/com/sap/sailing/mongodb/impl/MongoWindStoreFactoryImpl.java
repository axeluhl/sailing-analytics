package com.sap.sailing.mongodb.impl;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mongodb.MongoException;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoWindStore;
import com.sap.sailing.mongodb.MongoWindStoreFactory;

public class MongoWindStoreFactoryImpl implements MongoWindStoreFactory, BundleActivator {
    private static final Logger logger = Logger.getLogger(MongoWindStoreFactoryImpl.class.getName());
    
    private static MongoWindStoreFactory defaultInstance;
    
    private String defaultHostName;
    private Integer defaultPort;
    private String defaultDatabaseName;
    
    @Override
    public MongoWindStore getMongoWindStore(MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException {
        return getMongoWindStore(defaultHostName, defaultPort, defaultDatabaseName, mongoObjectFactory);
    }

    @Override
    public MongoWindStore getMongoWindStore(String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException {
        return getMongoWindStore(defaultHostName, defaultPort, dbName, mongoObjectFactory);
    }

    @Override
    public MongoWindStore getMongoWindStore(int port, String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException {
        return getMongoWindStore(defaultHostName, port, dbName, mongoObjectFactory);
    }

    @Override
    public MongoWindStore getMongoWindStore(String hostname, int port, String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException,
            MongoException {
        return new MongoWindStoreImpl(hostname, port, dbName, mongoObjectFactory);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        defaultInstance = this;
        defaultDatabaseName = context.getProperty("mongo.dbName");
        if (defaultDatabaseName == null) {
            defaultDatabaseName = DEFAULT_DB_NAME;
        } else {
            logger.log(Level.INFO, "found mongo.dbName="+defaultDatabaseName);
        }
        defaultHostName = context.getProperty("mongo.hostname");
        if (defaultHostName == null) {
            defaultHostName = "localhost";
        } else {
            logger.log(Level.INFO, "found mongo.hostname="+defaultHostName);
        }
        defaultPort = context.getProperty("mongo.port") != null ? Integer.valueOf(context.getProperty("mongo.port")) : 27017;
        logger.log(Level.INFO, "Using port "+defaultPort+" as default for Mongo wind stores");
    }
    
    public static MongoWindStoreFactory getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
