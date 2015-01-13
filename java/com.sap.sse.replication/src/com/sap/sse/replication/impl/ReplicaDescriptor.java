package com.sap.sse.replication.impl;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Describes a replica by remembering its IP address as well as the replication time and a UUID. Hash code and equality
 * are based solely on the UUID.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 * 
 */
public class ReplicaDescriptor implements Serializable {
    private static final long serialVersionUID = -5451556877949921454L;

    private final UUID uuid;
    private final InetAddress ipAddress;
    private final TimePoint registrationTime;
    private final String additionalInformation;

    /**
     * Sets the registration time to now.
     */
    public ReplicaDescriptor(InetAddress ipAddress, UUID serverUuid, String additionalInformation) {
        this.uuid = serverUuid;
        this.registrationTime = MillisecondsTimePoint.now();
        this.ipAddress = ipAddress;
        this.additionalInformation = additionalInformation;
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
        ReplicaDescriptor other = (ReplicaDescriptor) obj;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }
    
    public String toString() {
        return ""+uuid+": "+ipAddress+" ("+additionalInformation+")";
    }

}
