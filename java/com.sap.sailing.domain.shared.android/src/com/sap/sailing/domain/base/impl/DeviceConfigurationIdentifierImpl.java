package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.DeviceConfigurationIdentifier;

public class DeviceConfigurationIdentifierImpl implements DeviceConfigurationIdentifier {
    
    private final String clientIdentifier;
    
    public DeviceConfigurationIdentifierImpl(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public String toString() {
        return "TabletConfigurationIdentifierImpl [clientIdentifier=" + clientIdentifier + "]";
    }

}
