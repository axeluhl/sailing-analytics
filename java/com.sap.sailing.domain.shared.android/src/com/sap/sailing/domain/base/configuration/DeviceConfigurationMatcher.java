package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMapImpl;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sse.common.IsManagedByCache;

/**
 * <p>
 * Classes implementing this interface are matching {@link DeviceConfigurationIdentifier}s to associate them with a
 * {@link DeviceConfiguration}.
 * </p>
 * 
 * <p>The rank (see {@link DeviceConfigurationMatcher#getMatchingRank()}) is used as a priority when multiple matchers
 *  are matching a {@link DeviceConfigurationIdentifier} (see also {@link DeviceConfigurationMapImpl}) (lower wins).</p> 
 */
public interface DeviceConfigurationMatcher extends IsManagedByCache<SharedDomainFactory>, Serializable {

    /**
     * Returns the kind of matching used by this matcher.
     * 
     * @return the {@link Type}.
     */
    DeviceConfigurationMatcherType getMatcherType();

    /**
     * Gets the matcher's rank.
     * 
     * @return the rank.
     */
    int getMatchingRank();

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
