package com.sap.sse.mongodb;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mongodb.MongoClientURI;
import com.sap.sse.mongodb.internal.MongoDBServiceImpl;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    @Override
    public void start(BundleContext context) throws Exception {
        MongoDBConfiguration defaultConfiguration = MongoDBConfiguration.getDefaultConfiguration();
        final MongoDBConfiguration newConfiguration;
        final String mongoClientURI = context.getProperty(MongoDBConfiguration.MONGO_URI);
        if (mongoClientURI != null) {
            logger.info("found "+MongoDBConfiguration.MONGO_URI+"="+mongoClientURI);
            newConfiguration = new MongoDBConfiguration(new MongoClientURI(
                    context.getProperty(MongoDBConfiguration.MONGO_URI), MongoDBConfiguration.getDefaultOptionsBuilder()));
        } else {
            final String databaseNameProperty = context.getProperty(MongoDBConfiguration.MONGO_DB_NAME);
            final String databaseName;
            if (databaseNameProperty == null) {
                databaseName = defaultConfiguration.getMongoClientURI().getDatabase();
            } else {
                databaseName = databaseNameProperty;
                logger.log(Level.INFO, "found "+MongoDBConfiguration.MONGO_DB_NAME+"="+databaseName);
            }
            final String hostNameFromProperty = context.getProperty(MongoDBConfiguration.MONGO_HOSTNAME);
            final String hostName;
            final String[] primaryHostAndPort = defaultConfiguration.getMongoClientURI().getHosts().get(0).split(":");
            if (hostNameFromProperty != null) {
                logger.log(Level.INFO, "found "+MongoDBConfiguration.MONGO_HOSTNAME+"="+hostNameFromProperty);
                hostName = hostNameFromProperty;
            } else {
                hostName = defaultConfiguration.getMongoClientURI().getHosts().get(0).split(":")[0];
            }
            if (context.getProperty(MongoDBConfiguration.MONGO_PORT) == null) {
                if (primaryHostAndPort.length > 1) {
                    newConfiguration = new MongoDBConfiguration(hostName, Integer.valueOf(primaryHostAndPort[1]), databaseName);
                } else {
                    newConfiguration = new MongoDBConfiguration(hostName, databaseName);
                }
            } else {
                final String portAsString = context.getProperty(MongoDBConfiguration.MONGO_PORT);
                logger.log(Level.INFO, "found "+MongoDBConfiguration.MONGO_PORT+"="+portAsString);
                newConfiguration = new MongoDBConfiguration(hostName, Integer.valueOf(portAsString), databaseName);
            }
        }
        ((MongoDBServiceImpl) MongoDBService.INSTANCE).setConfiguration(newConfiguration);
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        
    }

}
