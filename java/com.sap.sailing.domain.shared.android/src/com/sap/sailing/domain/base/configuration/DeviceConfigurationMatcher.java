package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMapImpl;

/**
 * <p>
 * Classes implementing this interface are matching {@link DeviceConfigurationIdentifier}s to associate them with a
 * {@link DeviceConfiguration}.
 * </p>
 * 
 * <p>The rank (see {@link DeviceConfigurationMatcher#getMatchingRank()}) is used as a priority when multiple matchers
 *  are matching a {@link DeviceConfigurationIdentifier} (see also {@link DeviceConfigurationMapImpl}).</p> 
 */
public interface DeviceConfigurationMatcher extends IsManagedBySharedDomainFactory, Serializable {
    final static int RANK_SINGLE = 1;
    final static int RANK_MULTI = 2;
    final static int RANK_ANY = 3;

    public enum Type {
        SINGLE(RANK_SINGLE), MULTI(RANK_MULTI), ANY(RANK_ANY);

        private int matchingRank;

        private Type(int rank) {
            this.matchingRank = rank;
        }

        public int getRank() {
            return matchingRank;
        }
    }

    /**
     * Returns the kind of matching used by this matcher.
     * 
     * @return the {@link Type}.
     */
    Type getMatcherType();

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
