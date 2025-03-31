package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.session.ClientSession;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.mongodb.impl.DatabaseImpl;
import com.sap.sse.landscape.mongodb.impl.SimpleMongoEndpointImpl;

/**
 * A MongoDB endpoint that an application can connect to. It can produce a {@link URI} the client can use to connect to,
 * e.g., with a {@code ConnectionString}. The endpoint can be a standalone MongoDB instance, represented by a single
 * {@link MongoProcess}, or it may be a {@link MongoReplicaSet replica set}.<p>
 * 
 * Two MongoDB endpoints are {@link Object#equals(Object) equal} if they are of the same type (e.g., both a replica set,
 * or both a single instance) and have at least one instance in common.<p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MongoEndpoint {
    static MongoEndpoint of(String hostname, int port) {
        return new SimpleMongoEndpointImpl(hostname, port);
    }
    
    static MongoEndpoint of(String hostname, int port, String replicaSetName) {
        return new SimpleMongoEndpointImpl(hostname, port, replicaSetName);
    }
    
    /**
     * When invoked on a {@link MongoProcess} that is not currently equipped with a public IP address, a
     * {@link NullPointerException} will result. Consider using {@link #getURI(Optional, Optional)} to wait for
     * a public IP address to become available if the instance is still booting up.
     */
    URI getURI(Optional<Database> optionalDb) throws URISyntaxException;
    
    URI getURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException;
    
    default URI getURI(Optional<Database> optionalDb, String hostname, int port, Optional<String> replicaSetName) throws URISyntaxException {
        final StringBuilder sb = new StringBuilder();
        sb.append("mongodb://");
        sb.append(hostname);
        sb.append(":");
        sb.append(port);
        sb.append("/");
        optionalDb.ifPresent(db->sb.append(db.getName()));
        replicaSetName.ifPresent(rsName->appendReplicaSetParametersToURI(rsName, sb));
        return new URI(sb.toString());
    }
    
    default void appendReplicaSetParametersToURI(String replicaSetName, StringBuilder uriStringBuilder) {
        uriStringBuilder.append("?replicaSet=");
        uriStringBuilder.append(replicaSetName);
        uriStringBuilder.append("&retryWrites=true&readPreference=nearest");
    }
    
    /**
     * Lists all MongoDB databases available in this end point
     */
    Iterable<MongoDatabase> getMongoDatabases() throws URISyntaxException;

    MongoDatabase getMongoDatabase(String dbName) throws URISyntaxException;

    /**
     * Imports a {@link Database} from another {@link MongoReplicaSet} which must be different from {@code this}
     * replica set (otherwise an {@link IllegalArgumentException} is thrown). Before dropping / removing the
     * {@code from} database, callers may want to compare the {@link Database#getMD5Hash() hash} of {@code from}
     * with that of the {@link Database} returned by this method.
     */
    MongoDatabase importDatabase(MongoDatabase from) throws URISyntaxException;

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
     * {@link NullPointerException} will result. Consider using {@link #getConnectionString(Optional, Optional)} to wait for
     * a public IP address to become available if the instance is still booting up.
     */
    ConnectionString getConnectionString(Optional<Database> optionalDb) throws URISyntaxException;

    ConnectionString getConnectionString(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException;

    /**
     * When invoked on a {@link MongoProcess} that is not currently equipped with a public IP address, a
     * {@link NullPointerException} will result. Consider using {@link #getClient(Optional)} to wait for
     * a public IP address to become available if the instance is still booting up.
     */
    MongoClient getClient() throws URISyntaxException;

    MongoClient getClient(Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException;

    ClientSession getClientSession() throws URISyntaxException;
    
    /**
     * Compures an MD5 hash of a single database within this end point. Those hashes are computed using the following
     * MongoDB command:
     * 
     * <pre>
     *      db.runCommand({dbHash: 1})</pre>
     * This produces output of the form
     * 
     * <pre>
     *    {
     *        "host" : "WDFN34199197A",
     *        "collections" : {
     *
     *        },
     *        "capped" : [ ],
     *        "uuids" : {
     *
     *        },
     *        "md5" : "d41d8cd98f00b204e9800998ecf8427e",
     *        "timeMillis" : 1,
     *        "ok" : 1
     *    }</pre>
     * of which this method extracts the "md5" field.
     */
    default String getMD5Hash(String databaseName) throws URISyntaxException {
        return getMD5Hash(getMongoDatabase(databaseName));
    }
    
    /**
     * See {@link #getMD5Hash(String)}
     */
    String getMD5Hash(MongoDatabase database) throws URISyntaxException;

    /**
     * Returns a fully-configured {@link Database} that uses this {@link MongoEndpoint endpoint} to talk to the database.
     * The result's {@link Database#getEndpoint()} method returns {@code this} object.
     */
    default Database getDatabase(String name) {
        return new DatabaseImpl(this, name);
    }
    
    boolean equals(Object o);
    
    int hashCode();
}
