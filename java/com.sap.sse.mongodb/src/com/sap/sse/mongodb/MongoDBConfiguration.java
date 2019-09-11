package com.sap.sse.mongodb;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.sap.sse.mongodb.internal.MongoDBServiceImpl;

/**
 * A readonly Mongo Database connection configuration
 * @author Frank
 *
 */
public class MongoDBConfiguration {

    static final String MONGO_PORT = "mongo.port";
    static final String MONGO_HOSTNAME = "mongo.host";
    static final String MONGO_DB_NAME = "mongo.dbName";
    
    /**
     * The system property by this name may contain a MongoClientURI in its string representation:
     * <pre>
     *   mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database[.collection]][?options]]
     * </pre>
     * See also {@link MongoClientURI}.
     */
    static final String MONGO_URI = "mongo.uri";
    
    private static String DEFAULT_DB_NAME = "winddb"; 
    private static String DEFAULT_TEST_DB_NAME = "winddbTest"; 

    private final MongoClientURI mongoClientURI;

    public static MongoDBConfiguration getDefaultConfiguration() {
        final MongoDBConfiguration result;
        if (System.getProperty(MONGO_URI) != null) {
            result = new MongoDBConfiguration(new MongoClientURI(System.getProperty(MONGO_URI)));
        } else {
            String defaultHostName = "127.0.0.1";
            if (System.getProperty(MONGO_HOSTNAME) != null) {
                defaultHostName = System.getProperty(MONGO_HOSTNAME);
            } else {
                if (System.getenv("MONGODB_HOST") != null) {
                    defaultHostName = System.getenv("MONGODB_HOST");
                }
            }
            String defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_DB_NAME);
            if (System.getProperty(MONGO_PORT) != null) {
                result = new MongoDBConfiguration(defaultHostName, Integer.valueOf(System.getProperty(MONGO_PORT).trim()), defaultDatabaseName);
            } else {
                if (System.getenv("MONGODB_PORT") != null) {
                    result = new MongoDBConfiguration(defaultHostName, Integer.valueOf(System.getenv("MONGODB_PORT").trim()), defaultDatabaseName);
                } else {
                    result = new MongoDBConfiguration(defaultHostName, defaultDatabaseName);
                }
            }
        }
        return result;
    }

    public static MongoDBConfiguration getDefaultTestConfiguration() {
        final MongoDBConfiguration result;
        if (System.getProperty(MONGO_URI) != null) {
            result = new MongoDBConfiguration(new MongoClientURI(System.getProperty(MONGO_URI)));
        } else {
            String defaultHostName = "127.0.0.1";
            if (System.getProperty(MONGO_HOSTNAME) != null) {
                defaultHostName = System.getProperty(MONGO_HOSTNAME);
            } else {
                if (System.getenv("MONGODB_HOST") != null) {
                    defaultHostName = System.getenv("MONGODB_HOST");
                }
            }
            String defaultDatabaseName = System.getProperty(MONGO_DB_NAME, DEFAULT_TEST_DB_NAME);
            if (System.getProperty(MONGO_PORT) != null) {
                result = new MongoDBConfiguration(defaultHostName, Integer.valueOf(System.getProperty(MONGO_PORT).trim()), defaultDatabaseName);
            } else {
                if (System.getenv("MONGODB_PORT") != null) {
                    result = new MongoDBConfiguration(defaultHostName, Integer.valueOf(System.getenv("MONGODB_PORT").trim()), defaultDatabaseName);
                } else {
                    result = new MongoDBConfiguration(defaultHostName, defaultDatabaseName);
                }
            }
        }
        return result;
    }

    public MongoDBConfiguration(String mongoClientURIAsString) {
        this.mongoClientURI = new MongoClientURI(mongoClientURIAsString, getDefaultOptionsBuilder());
    }

    public MongoDBConfiguration(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
    }

    public MongoDBConfiguration(String hostName, String databaseName) {
        this.mongoClientURI = new MongoClientURI("mongodb://"+hostName+"/"+databaseName, getDefaultOptionsBuilder());
    }

    public MongoDBConfiguration(String hostName, int port, String databaseName) {
        this.mongoClientURI = new MongoClientURI("mongodb://"+hostName+":"+port+"/"+databaseName);
    }

    /**
     * Creates and returns a DB service for this configuration
     */
    public MongoDBService getService() {
        return new MongoDBServiceImpl(this);
    }

    public MongoClientURI getMongoClientURI() {
        return mongoClientURI;
    }

    public static MongoClientOptions.Builder getDefaultOptionsBuilder() {
        return new Builder().readPreference(ReadPreference.primaryPreferred());
    }

    public String getDatabaseName() {
        return getMongoClientURI().getDatabase();
    }

    public String getHostname() {
        return getMongoClientURI().getHosts().get(0).split(":")[0];
    }

    public int getPort() {
        int result = ServerAddress.defaultPort();
        if (!getMongoClientURI().getHosts().isEmpty() && getMongoClientURI().getHosts().get(0).split(":").length > 1) {
            result = Integer.valueOf(getMongoClientURI().getHosts().get(0).split(":")[1]);
        }
        return result;
    }
}
