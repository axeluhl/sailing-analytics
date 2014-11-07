package com.sap.sailing.domain.base.configuration.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sse.IsManagedByCache;

/**
 * Matches a set of {@link DeviceConfigurationIdentifier}s.
 */
public class DeviceConfigurationMatcherMulti implements DeviceConfigurationMatcher {
    
    private static final long serialVersionUID = 2372299957258702516L;
    
    private final List<String> clientIdentifiers;
    
    public DeviceConfigurationMatcherMulti(List<String> matchingClientIdentifiers) {
        this.clientIdentifiers = new ArrayList<String>(matchingClientIdentifiers);
        Collections.sort(this.clientIdentifiers);
    }

    @Override
    public DeviceConfigurationMatcherType getMatcherType() {
        return DeviceConfigurationMatcherType.MULTI;
    }

    @Override
    public int getMatchingRank() {
        return getMatcherType().getRank();
    }

    @Override
    public boolean matches(DeviceConfigurationIdentifier identifier) {
        return this.clientIdentifiers.contains(identifier.getClientIdentifier());
    }

    public Iterable<String> getClientIdentifiers() {
        return clientIdentifiers;
    }

    @Override
    public Serializable getMatcherIdentifier() {
        return String.format("%s%s", DeviceConfigurationMatcherMulti.class.getSimpleName(), clientIdentifiers.toString());
    }

    @Override
    public String toString() {
        return "DeviceConfigurationMatcherMulti [clientIdentifiers=" + clientIdentifiers + "]";
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getOrCreateDeviceConfigurationMatcher(getMatcherType(), clientIdentifiers);
    }
    
}
