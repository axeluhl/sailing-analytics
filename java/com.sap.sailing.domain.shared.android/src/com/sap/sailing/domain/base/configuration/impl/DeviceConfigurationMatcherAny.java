package com.sap.sailing.domain.base.configuration.impl;

import java.io.Serializable;

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
    public Type getMatcherType() {
        return Type.ANY;
    }

    @Override
    public int getMatchingRank() {
        return getMatcherType().getRank();
    }

    @Override
    public Serializable getMatcherIdentifier() {
        return DeviceConfigurationMatcherAny.class.getSimpleName();
    }

}
