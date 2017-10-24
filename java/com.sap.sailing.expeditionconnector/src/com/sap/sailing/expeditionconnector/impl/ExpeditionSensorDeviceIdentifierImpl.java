package com.sap.sailing.expeditionconnector.impl;

import java.util.UUID;

import com.sap.sailing.expeditionconnector.ExpeditionSensorDeviceIdentifier;

public class ExpeditionSensorDeviceIdentifierImpl extends AbstractExpeditionDeviceIdentifierImpl implements ExpeditionSensorDeviceIdentifier {
    private static final long serialVersionUID = -4049961972156611640L;
    
    public ExpeditionSensorDeviceIdentifierImpl(UUID id) {
        super(id);
    }

    @Override
    public String getIdentifierType() {
        return ExpeditionSensorDeviceIdentifier.TYPE;
    }
}
