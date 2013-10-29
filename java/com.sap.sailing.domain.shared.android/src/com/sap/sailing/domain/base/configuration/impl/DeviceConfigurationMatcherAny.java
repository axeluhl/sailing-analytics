package com.sap.sailing.domain.base.configuration.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;

/**
 * Matches all {@link DeviceConfigurationIdentifier}.
 */
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

    @Override
    public IsManagedBySharedDomainFactory resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getOrCreateDeviceConfigurationMatcher(getMatcherType(), Collections.<String>emptyList());
    }

}
