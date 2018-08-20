package com.sap.sse.mongodb;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.mongodb.internal.MongoDBServiceImpl;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    @Override
    public void start(BundleContext context) throws Exception {

        MongoDBConfiguration defaultConfiguration = MongoDBConfiguration.getDefaultConfiguration(); 
        
        String databaseName = context.getProperty(MongoDBConfiguration.MONGO_DB_NAME);
        if (databaseName == null) {
            databaseName = defaultConfiguration.getDatabaseName();
        } else {
            logger.log(Level.INFO, "found "+MongoDBConfiguration.MONGO_DB_NAME+"="+databaseName);
        }
        String hostName = context.getProperty(MongoDBConfiguration.MONGO_HOSTNAME);
        if (hostName == null) {
            hostName = defaultConfiguration.getHostName();
        } else {
            logger.log(Level.INFO, "found "+MongoDBConfiguration.MONGO_HOSTNAME+"="+hostName);
        }
        int port;
        if(context.getProperty(MongoDBConfiguration.MONGO_PORT) == null) {
            port = defaultConfiguration.getPort();
        } else {
            port = Integer.valueOf(context.getProperty(MongoDBConfiguration.MONGO_PORT));
            logger.log(Level.INFO, "found "+MongoDBConfiguration.MONGO_PORT+"="+port);
        }

        MongoDBConfiguration newConfiguration = new MongoDBConfiguration(hostName, port, databaseName); 
        ((MongoDBServiceImpl) MongoDBService.INSTANCE).setConfiguration(newConfiguration);
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        
    }

}
