package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sse.common.IsManagedByCache;

/**
 * <p>
 * Classes implementing this interface are matching {@link DeviceConfigurationIdentifier}s to associate them with a
 * {@link DeviceConfiguration}.
 * </p> 
 */
public interface DeviceConfigurationMatcher extends IsManagedByCache<SharedDomainFactory>, Serializable {
    /**
     * Returns <code>true</code> if this {@link DeviceConfigurationMatcher} matches the given
     * {@link DeviceConfigurationIdentifier}; otherwise <code>false</code>.
     * 
     * @param identifier
     */
    boolean matches(DeviceConfigurationIdentifier identifier);

    /**
     * Gets this matcher's identifier.
     * 
     * @return the identifier.
     */
    Serializable getMatcherIdentifier();
}
