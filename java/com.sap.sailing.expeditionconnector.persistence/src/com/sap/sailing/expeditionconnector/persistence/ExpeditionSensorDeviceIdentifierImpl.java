package com.sap.sailing.expeditionconnector.persistence;

import java.util.UUID;

import com.sap.sailing.expeditionconnector.ExpeditionSensorDeviceIdentifier;
import com.sap.sailing.expeditionconnector.persistence.impl.AbstractExpeditionDeviceIdentifierImpl;

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
