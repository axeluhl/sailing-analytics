package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.session.ClientSession;
import com.sap.sse.common.Duration;

/**
 * A MongoDB endpoint that an application can connect to. It can produce a {@link URI} the client can use to connect to,
 * e.g., with a {@code MongoClientURI}. The endpoint can be a standalong MongoDB instance, represented by a single
 * {@link MongoProcess}, or it may be a {@link MongoReplicaSet replica set}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MongoEndpoint {
    /**
     * When invoked on a {@link MongoProcess} that is not currently equipped with a public IP address, a
     * {@link NullPointerException} will result. Consider using {@link #getURI(Optional, Optional)} to wait for
     * a public IP address to become available if the instance is still booting up.
     */
    URI getURI(Optional<Database> optionalDb) throws URISyntaxException;
    
    URI getURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException;
    
    Iterable<MongoDatabase> getMongoDatabases() throws URISyntaxException;

    MongoDatabase getMongoDatabase(String dbName) throws URISyntaxException;

    /**
     * Imports a {@link Database} from another {@link MongoReplicaSet} which must be different from {@code this}
     * replica set (otherwise an {@link IllegalArgumentException} is thrown). Before dropping / removing the
     * {@code from} database, callers may want to compare the {@link Database#getMD5Hash() hash} of {@code from}
     * with that of the {@link Database} returned by this method.
     */
    MongoDatabase importDatabase(MongoDatabase from);

    boolean isInReplicaSet() throws URISyntaxException;
    
    default boolean isReplicaSet() {
        return this instanceof MongoReplicaSet;
    }
    
    default MongoProcess asMongoProcess() {
        return (MongoProcess) this;
    }

    default MongoReplicaSet asMongoReplicaSet() {
        return (MongoReplicaSet) this;
    }

    /**
     * When invoked on a {@link MongoProcess} that is not currently equipped with a public IP address, a
     * {@link NullPointerException} will result. Consider using {@link #getMongoClientURI(Optional, Optional)} to wait for
     * a public IP address to become available if the instance is still booting up.
     */
    MongoClientURI getMongoClientURI(Optional<Database> optionalDb) throws URISyntaxException;

    MongoClientURI getMongoClientURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException;

    /**
     * When invoked on a {@link MongoProcess} that is not currently equipped with a public IP address, a
     * {@link NullPointerException} will result. Consider using {@link #getClient(Optional)} to wait for
     * a public IP address to become available if the instance is still booting up.
     */
    MongoClient getClient() throws URISyntaxException;

    MongoClient getClient(Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException;

    ClientSession getClientSession() throws URISyntaxException;
    
    default String getMD5Hash(String databaseName) throws URISyntaxException {
        return getMD5Hash(getMongoDatabase(databaseName));
    }
    
    String getMD5Hash(MongoDatabase database) throws URISyntaxException;
}
