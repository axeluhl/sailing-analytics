package com.sap.sailing.server.replication;

import java.net.URL;

import javax.jms.Topic;

/**
 * Identifies a master server instance from which a replica can obtain an initial load and continuous updates.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public interface ReplicationMasterDescriptor {

    URL getReplicationRegistrationRequestURL();

    /**
     * Obtains the remote master's replication topic for a replica to subscribe to.
     */
    Topic getReplicationTopic();

    URL getInitialLoadURL();
}
