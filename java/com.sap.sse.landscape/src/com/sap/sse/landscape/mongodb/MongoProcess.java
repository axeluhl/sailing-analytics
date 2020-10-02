package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface MongoProcess extends Process<RotatingFileBasedLog, MongoMetrics> {
    int DEFAULT_PORT = 27017;
    boolean isHidden();
    int getPriority();
    int getVotes();
    
    default URI getSingleInstanceConnectionURI(Optional<Database> optionalDb) throws URISyntaxException {
        final StringBuilder sb = new StringBuilder();
        sb.append("mongodb://");
        sb.append(getHost().getPublicAddress().getCanonicalHostName());
        sb.append(":");
        sb.append(getPort());
        sb.append("/");
        optionalDb.ifPresent(db->sb.append(db.getName()));
        return new URI(sb.toString());
    }
}
