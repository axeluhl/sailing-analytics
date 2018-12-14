package com.sap.sse.replication.impl;

import java.net.InetAddress;
import java.util.UUID;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.ReplicaDescriptor;
import com.sap.sse.replication.Replicable;

/**
 * Describes a replica by remembering its IP address as well as the replication time and a UUID. Hash code and equality
 * are based solely on the UUID.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class ReplicaDescriptorImpl implements ReplicaDescriptor {
    private static final long serialVersionUID = -5451556877949921454L;

    private final UUID uuid;
    private final InetAddress ipAddress;
    private final TimePoint registrationTime;
    private final String additionalInformation;
    private final String[] replicableIdsAsStrings;

    /**
     * Sets the registration time to now.
     */
    public ReplicaDescriptorImpl(InetAddress ipAddress, UUID serverUuid, String additionalInformation, String[] replicableIdsAsStrings) {
        assert replicableIdsAsStrings != null && replicableIdsAsStrings.length > 0;
        this.uuid = serverUuid;
        this.registrationTime = MillisecondsTimePoint.now();
        this.ipAddress = ipAddress;
        this.additionalInformation = additionalInformation;
        this.replicableIdsAsStrings = replicableIdsAsStrings;
    }

    public UUID getUuid() {
        return uuid;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public TimePoint getRegistrationTime() {
        return registrationTime;
    }
    
    public String getAdditionalInformation() {
        return additionalInformation;
    }

    /**
     * The {@link Replicable#getId() IDs} of the replicables that the replica represented by this descriptor
     * has requested from the master for replication. The master may send operations for a superset of those
     * replicables in case other replicas have requested replication for other replicables. Therefore, the
     * replica must filter the operations received for those replicable IDs it has been requesting replication
     * for.
     */
    public String[] getReplicableIdsAsStrings() {
        return replicableIdsAsStrings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReplicaDescriptorImpl other = (ReplicaDescriptorImpl) obj;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }
    
    public String toString() {
        return ""+uuid+": "+ipAddress+" ("+additionalInformation+") for replicables "+String.join(", ", getReplicableIdsAsStrings());
    }

}
