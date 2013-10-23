package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;

public enum DeviceConfigurationMatcherAny implements DeviceConfigurationMatcher {
    INSTANCE;

    private static final long serialVersionUID = 4574449656453672610L;

    @Override
    public boolean matches(DeviceConfigurationIdentifier identifier) {
        return true;
    }

    @Override
    public int getMatchingRank() {
        return RANK_ANY;
    }

}
