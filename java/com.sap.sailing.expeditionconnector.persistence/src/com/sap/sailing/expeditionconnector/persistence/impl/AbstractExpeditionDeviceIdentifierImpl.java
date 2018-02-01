package com.sap.sailing.expeditionconnector.persistence.impl;

import java.util.UUID;

import com.sap.sailing.expeditionconnector.ExpeditionDeviceIdentifier;

public abstract class AbstractExpeditionDeviceIdentifierImpl implements ExpeditionDeviceIdentifier {
    private static final long serialVersionUID = -6605059302775505785L;
    private final UUID id;
    
    public AbstractExpeditionDeviceIdentifierImpl(UUID id) {
        super();
        this.id = id;
    }

    @Override
    public String getStringRepresentation() {
        return id.toString();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ExpeditionDeviceIdentifier) &&
            ((ExpeditionDeviceIdentifier) obj).getIdentifierType().equals(getIdentifierType()) &&
            ((ExpeditionDeviceIdentifier) obj).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return 9478 ^ getIdentifierType().hashCode() ^ getId().hashCode();
    }
}
