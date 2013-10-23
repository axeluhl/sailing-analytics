package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;

public class DeviceConfigurationMatcherSingle implements DeviceConfigurationMatcher {
    
    private static final long serialVersionUID = -3662453614818373801L;
    private final String clientIdentifier;

    public DeviceConfigurationMatcherSingle(String matchingClientIdentifier) {
        this.clientIdentifier = matchingClientIdentifier;
    }

    @Override
    public boolean matches(DeviceConfigurationIdentifier identifier) {
        return this.clientIdentifier.equals(identifier.getClientIdentifier());
    }

    @Override
    public int getMatchingRank() {
        return RANK_SINGLE;
    }
    
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientIdentifier == null) ? 0 : clientIdentifier.hashCode());
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
        DeviceConfigurationMatcherSingle other = (DeviceConfigurationMatcherSingle) obj;
        if (clientIdentifier == null) {
            if (other.clientIdentifier != null)
                return false;
        } else if (!clientIdentifier.equals(other.clientIdentifier))
            return false;
        return true;
    }

}
