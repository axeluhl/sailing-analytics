package com.sap.sailing.selenium.test;

import com.mongodb.DB;
import com.mongodb.MongoException;

import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.mongodb.MongoDBService;

public class DatabaseHelper {
    public static String TEST_DB_NAME = "winddbTest"; 
    
    public static void dropDatabase() throws MongoException {
        dropDatabase(getTestConfiguration());
    }
    
    public static void dropDatabase(MongoDBConfiguration configuration) throws MongoException {
        DatabaseHelper helper = new DatabaseHelper(configuration);
        DB database = helper.getDatabase();
        
        database.dropDatabase();
    }
    
    private static MongoDBConfiguration getTestConfiguration() {
        String host = System.getProperty(MongoDBConfiguration.MONGO_HOSTNAME, "127.0.0.1");
        int port = Integer.valueOf(System.getProperty(MongoDBConfiguration.MONGO_PORT, "27017"));
                
        return new MongoDBConfiguration(host, port, TEST_DB_NAME);
    }
    
    private final MongoDBConfiguration configuration;
    
    private final MongoDBService service;
    
    private final DB database;
    
    public DatabaseHelper() {
        this(getTestConfiguration());
    }
    
    public DatabaseHelper(MongoDBConfiguration configuration) {
        this.configuration = configuration;
        this.service = getDBConfiguration().getService();
        this.database = this.service.getDB();
    }
    
    public MongoDBConfiguration getDBConfiguration() {
        return this.configuration;
    }
    
    public MongoDBService getService() {
        return this.service;
    }
    
    public DB getDatabase() {
        return this.database;
    }
}
