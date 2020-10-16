package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Named;

public interface Database extends Named {
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
}
