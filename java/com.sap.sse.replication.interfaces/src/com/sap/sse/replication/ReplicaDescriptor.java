package com.sap.sse.replication;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

import com.sap.sse.common.TimePoint;

/**
 * Describes a replica by remembering its IP address as well as the replication time and a UUID. Hash code and equality
 * are based solely on the UUID.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public interface ReplicaDescriptor extends Serializable {
    UUID getUuid();

    InetAddress getIpAddress();

    TimePoint getRegistrationTime();
    
    String getAdditionalInformation();

    /**
     * The {@link Replicable#getId() IDs} of the replicables that the replica represented by this descriptor
     * has requested from the master for replication. The master may send operations for a superset of those
     * replicables in case other replicas have requested replication for other replicables. Therefore, the
     * replica must filter the operations received for those replicable IDs it has been requesting replication
     * for.
     */
    String[] getReplicableIdsAsStrings();
}
