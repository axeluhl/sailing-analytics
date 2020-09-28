package com.sap.sse.landscape.mongodb;

import java.net.URI;

public interface MongoReplicaSet {
    Iterable<MongoProcess> getInstances();
    
    /**
     * The {@code "mongodb://..."} URI that application use to connect to this replica set; not specific
     * to any particular database managed by this replica set; see also {@link Database#getConnectionURI()}.
     */
    URI getConnectionURI();
    
    /**
     * Imports a {@link Database} from another {@link MongoReplicaSet} which must be different from {@code this}
     * replica set (otherwise an {@link IllegalArgumentException} is thrown). Before dropping / removing the
     * {@code from} database, callers may want to compare the {@link Database#getMD5Hash() hash} of {@code from}
     * with that of the {@link Database} returned by this method.
     */
    Database importDatabase(Database from);
}
