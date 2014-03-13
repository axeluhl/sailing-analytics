package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;

public class DeviceConfigurationIdentifierImpl implements DeviceConfigurationIdentifier {

    private static final long serialVersionUID = 2785847392322741468L;
    
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
        return "DeviceConfigurationIdentifierImpl [clientIdentifier=" + clientIdentifier + "]";
    }

}
