package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Named;
import com.sap.sse.landscape.DefaultProcessConfigurationVariables;
import com.sap.sse.landscape.ProcessConfigurationVariable;
import com.sap.sse.landscape.UserDataProvider;

public interface Database extends UserDataProvider, Named {
    /**
     * Obtains the database-specific connection URI that clients can use to connect to the replica set hosting this
     * database. See also {@link MongoReplicaSet#getConnectionURI()}.
     */
    default URI getConnectionURI() throws URISyntaxException {
        return getEndpoint().getURI(Optional.of(this));
    }
    
    MongoEndpoint getEndpoint();
    
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
}
