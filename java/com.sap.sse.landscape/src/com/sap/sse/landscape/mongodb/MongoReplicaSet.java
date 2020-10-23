package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sap.sse.common.Named;

public interface MongoReplicaSet extends Named {
    Iterable<MongoProcess> getInstances();
    
    /**
     * The {@code "mongodb://..."} URI that application use to connect to this replica set; not specific
     * to any particular database managed by this replica set; see also {@link Database#getConnectionURI()}.
     */
    default URI getConnectionURI(Optional<Database> optionalDb) throws URISyntaxException {
        final StringBuilder result = new StringBuilder("mongodb://");
        final List<String> hostSpecs = new ArrayList<>();
        for (final MongoProcess mongoProcess : getInstances()) {
            final StringBuilder hostSpec = new StringBuilder();
            hostSpec.append(mongoProcess.getHost().getPublicAddress().getCanonicalHostName());
            if (mongoProcess.getPort() != MongoProcess.DEFAULT_PORT) {
                hostSpec.append(":");
                hostSpec.append(mongoProcess.getPort());
            }
            hostSpecs.add(hostSpec.toString());
        }
        result.append(String.join(",", hostSpecs));
        result.append("/");
        optionalDb.ifPresent(db->result.append(db.getName()));
        result.append("?replicaSet=");
        result.append(getName());
        result.append("&retryWrites=true");
        return new URI(result.toString());
    }
    
    void addReplica(MongoProcess newReplica);
    
    void removeReplica(MongoProcess replicaToRemove);
    
    Iterable<Database> getDatabases();
    
    Database getDatabase(String dbName);
    
    /**
     * Imports a {@link Database} from another {@link MongoReplicaSet} which must be different from {@code this}
     * replica set (otherwise an {@link IllegalArgumentException} is thrown). Before dropping / removing the
     * {@code from} database, callers may want to compare the {@link Database#getMD5Hash() hash} of {@code from}
     * with that of the {@link Database} returned by this method.
     */
    Database importDatabase(Database from);
}
