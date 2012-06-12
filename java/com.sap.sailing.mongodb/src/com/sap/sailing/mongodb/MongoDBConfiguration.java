package com.sap.sailing.mongodb;

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
        String defaultHostName = System.getProperty(MONGO_HOSTNAME, "127.0.0.1");
        int defaultPort = Integer.valueOf(System.getProperty(MONGO_PORT, "27017"));
        String defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_DB_NAME);
                
        return new MongoDBConfiguration(defaultHostName, defaultPort, defaultDatabaseName);
    }

    public static MongoDBConfiguration getDefaultTestConfiguration() {
        String defaultHostName = System.getProperty(MONGO_HOSTNAME, "127.0.0.1");
        int defaultPort = Integer.valueOf(System.getProperty(MONGO_PORT, "27017"));
        String defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_TEST_DB_NAME);
                
        return new MongoDBConfiguration(defaultHostName, defaultPort, defaultDatabaseName);
    }

    public MongoDBConfiguration(String hostName, int port, String databaseName) {
        this.hostName = hostName;
        this.port = port;
        this.databaseName = databaseName;
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
