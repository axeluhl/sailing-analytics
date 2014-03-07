package com.sap.sailing.domain.racelogtracking;

import java.util.UUID;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;

public class PingDeviceIdentifierImpl implements PingDeviceIdentifier {
    private static final long serialVersionUID = -4049961972156611640L;

    public static final String TYPE = "PING";
    
    private final UUID id;
    
    public PingDeviceIdentifierImpl() {
        this.id = UUID.randomUUID();
    }
    
    public PingDeviceIdentifierImpl(UUID id) {
        this.id = id;
    }

    @Override
    public String getIdentifierType() {
        return TYPE;
    }

    @Override
    public IsManagedBySharedDomainFactory resolve(SharedDomainFactory domainFactory) {
        //No resolving necessary, only relevant on the creating server
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getStringRepresentation() {
        return id.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PingDeviceIdentifier) {
            return id.equals(((PingDeviceIdentifier) obj).getId());
        }
        return false;
    }
}
