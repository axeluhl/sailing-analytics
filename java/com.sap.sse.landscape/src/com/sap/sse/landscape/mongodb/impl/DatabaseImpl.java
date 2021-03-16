package com.sap.sse.landscape.mongodb.impl;

import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoEndpoint;

public class DatabaseImpl extends NamedImpl implements Database {
    private static final Logger logger = Logger.getLogger(DatabaseImpl.class.getName());
    private static final long serialVersionUID = -3816220771822620058L;
    private final MongoEndpoint endpoint;

    public DatabaseImpl(MongoEndpoint endpoint, String name) {
        super(name);
        this.endpoint = endpoint;
    }

    @Override
    public MongoEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public void drop() throws URISyntaxException {
        logger.info("Dropping database "+this);
        getMongoDatabase().drop();
    }

    @Override
    public String toString() {
        return "DatabaseImpl [endpoint=" + endpoint + "]";
    }
}
