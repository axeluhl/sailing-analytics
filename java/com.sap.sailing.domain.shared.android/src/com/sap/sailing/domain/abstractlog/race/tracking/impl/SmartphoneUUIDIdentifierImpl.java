package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.race.tracking.SmartphoneUUIDIdentifier;


public class SmartphoneUUIDIdentifierImpl implements SmartphoneUUIDIdentifier {
    private static final long serialVersionUID = -1747763242191049428L;
    
    private final UUID uuid;

    public SmartphoneUUIDIdentifierImpl(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getIdentifierType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "UUID " + uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmartphoneUUIDIdentifier) {
            return ((SmartphoneUUIDIdentifier) obj).getUUID().equals(uuid);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
    
    @Override
    public String getStringRepresentation() {
        return uuid.toString();
    }
}
