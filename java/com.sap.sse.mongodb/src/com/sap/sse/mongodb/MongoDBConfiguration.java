package com.sap.sse.mongodb;

import com.sap.sse.mongodb.internal.MongoDBServiceImpl;

/**
 * A readonly Mongo Database connection configuration
 * @author Frank
 *
 */
public class MongoDBConfiguration {

    public static final String MONGO_PORT = "mongo.port";
    public static final String MONGO_HOSTNAME = "mongo.host";
    public static final String MONGO_DB_NAME = "mongo.dbName";

    private static String DEFAULT_DB_NAME = "winddb"; 
    private static String DEFAULT_TEST_DB_NAME = "winddbTest"; 

    private String hostName;
    private int port;
    private String databaseName;

    public static MongoDBConfiguration getDefaultConfiguration() {
        String defaultHostName = "127.0.0.1";
        if (System.getProperty(MONGO_HOSTNAME) != null) {
            defaultHostName = System.getProperty(MONGO_HOSTNAME);
        } else {
            if (System.getenv("MONGODB_HOST") != null) {
                defaultHostName = System.getenv("MONGODB_HOST");
            }
        }
        
        int defaultPort = 27017;
        if (System.getProperty(MONGO_PORT) != null) {
            defaultPort = Integer.valueOf(System.getProperty(MONGO_PORT).trim());
        } else {
            if (System.getenv("MONGODB_PORT") != null) {
                defaultPort = Integer.valueOf(System.getenv("MONGODB_PORT").trim());
            }
        }
        String defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_DB_NAME);
                
        return new MongoDBConfiguration(defaultHostName, defaultPort, defaultDatabaseName);
    }

    public static MongoDBConfiguration getDefaultTestConfiguration() {
        String defaultHostName = "127.0.0.1";
        if (System.getProperty(MONGO_HOSTNAME) != null) {
            defaultHostName = System.getProperty(MONGO_HOSTNAME);
        } else {
            if (System.getenv("MONGODB_HOST") != null) {
                defaultHostName = System.getenv("MONGODB_HOST");
            }
        }
        
        int defaultPort = 27017;
        if (System.getProperty(MONGO_PORT) != null) {
            defaultPort = Integer.valueOf(System.getProperty(MONGO_PORT).trim());
        } else {
            if (System.getenv("MONGODB_PORT") != null) {
                defaultPort = Integer.valueOf(System.getenv("MONGODB_PORT").trim());
            }
        }
        String defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_TEST_DB_NAME);
                
        return new MongoDBConfiguration(defaultHostName, defaultPort, defaultDatabaseName);
    }

    public MongoDBConfiguration(String hostName, int port, String databaseName) {
        this.hostName = hostName;
        this.port = port;
        this.databaseName = databaseName;
    }
    
    /**
     * Creates and returns a DB service for this configuration
     */
    public MongoDBService getService() {
        return new MongoDBServiceImpl(this);
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

}
