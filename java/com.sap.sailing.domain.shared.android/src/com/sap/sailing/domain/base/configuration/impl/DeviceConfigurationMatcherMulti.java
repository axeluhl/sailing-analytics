package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;

public class DeviceConfigurationMatcherMulti implements DeviceConfigurationMatcher {
    
    private static final long serialVersionUID = 2372299957258702516L;
    
    private final List<String> clientIdentifiers;
    
    public DeviceConfigurationMatcherMulti(List<String> matchingClientIdentifiers) {
        this.clientIdentifiers = matchingClientIdentifiers;
    }

    @Override
    public boolean matches(DeviceConfigurationIdentifier identifier) {
        return this.clientIdentifiers.contains(identifier.getClientIdentifier());
    }

    @Override
    public int getMatchingRank() {
        return RANK_MULTI;
    }

    public List<String> getClientIdentifiers() {
        return clientIdentifiers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientIdentifiers == null) ? 0 : clientIdentifiers.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeviceConfigurationMatcherMulti other = (DeviceConfigurationMatcherMulti) obj;
        if (clientIdentifiers == null) {
            if (other.clientIdentifiers != null)
                return false;
        } else if (!clientIdentifiers.equals(other.clientIdentifiers))
            return false;
        return true;
    }
    
}
