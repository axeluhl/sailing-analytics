package com.sap.sailing.expeditionconnector.impl;

import java.util.UUID;

import com.sap.sailing.expeditionconnector.ExpeditionGpsDeviceIdentifier;

public class ExpeditionGpsDeviceIdentifierImpl extends AbstractExpeditionDeviceIdentifierImpl implements ExpeditionGpsDeviceIdentifier {
    private static final long serialVersionUID = -4049961972156611640L;
    
    public ExpeditionGpsDeviceIdentifierImpl(UUID id) {
        super(id);
    }

    @Override
    public String getIdentifierType() {
        return ExpeditionGpsDeviceIdentifier.TYPE;
    }
}
