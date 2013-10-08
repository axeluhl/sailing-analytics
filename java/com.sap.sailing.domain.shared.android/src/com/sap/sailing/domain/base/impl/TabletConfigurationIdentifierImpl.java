package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.TabletConfigurationIdentifier;

public class TabletConfigurationIdentifierImpl implements TabletConfigurationIdentifier {
    
    private final String clientIdentifier;
    
    public TabletConfigurationIdentifierImpl(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public String getClientIdentifier() {
        return clientIdentifier;
    }

}
