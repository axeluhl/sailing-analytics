package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * A MongoDB endpoint that an application can connect to. It can produce a {@link URI} the client can use to connect to,
 * e.g., with a {@code MongoClientURI}. The endpoint can be a standalong MongoDB instance, represented by a single
 * {@link MongoProcess}, or it may be a {@link MongoReplicaSet replica set}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MongoEndpoint {
    URI getURI(Optional<Database> optionalDb) throws URISyntaxException;
}
