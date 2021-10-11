package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Named;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.UserDataProvider;
import com.sap.sse.landscape.mongodb.impl.DatabaseImpl;

public interface Database extends UserDataProvider, Named {
    static final Logger logger = Logger.getLogger(Database.class.getName());
    
    /**
     * Obtains the database-specific connection URI that clients can use to connect to the replica set hosting this
     * database. See also {@link MongoReplicaSet#getConnectionURI()}.
     */
    default URI getConnectionURI() throws URISyntaxException {
        return getEndpoint().getURI(Optional.of(this));
    }
    
    MongoEndpoint getEndpoint();
    
    /**
     * See {@link MongoEndpoint#getMD5Hash(String)}
     */
    default String getMD5Hash() throws URISyntaxException {
        return getEndpoint().getMD5Hash(getMongoDatabase());
    }
    
    default MongoDatabase getMongoDatabase() throws URISyntaxException {
        return getEndpoint().getMongoDatabase(getName());
    }

    @Override
    default Map<ProcessConfigurationVariable, String> getUserData() {
        final Map<ProcessConfigurationVariable, String> result = new HashMap<>();
        try {
            result.put(DefaultProcessConfigurationVariables.MONGODB_URI, getConnectionURI().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Drops the database.
     */
    default void drop() throws URISyntaxException {
        logger.info("Dropping database "+this);
        getMongoDatabase().drop();
    }

    static Database of(MongoEndpoint mongoEndpoint, String databaseName) {
        return new DatabaseImpl(mongoEndpoint, databaseName);
    }

    /**
     * Produces a new object that equals this object, in particular using the same {@link #getEndpoint() MongoDB
     * endpoint}, except for the database {@link #getName() name} which is replaced by {@code differentName}.
     */
    default Database getWithDifferentName(String differentName) {
        return new DatabaseImpl(getEndpoint(), differentName);
    }
}
