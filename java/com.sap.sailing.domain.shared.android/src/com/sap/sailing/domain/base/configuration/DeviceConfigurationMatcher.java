package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;

public interface DeviceConfigurationMatcher extends IsManagedBySharedDomainFactory, Serializable {
    final static int RANK_SINGLE = 1;
    final static int RANK_MULTI = 2;
    final static int RANK_ANY = 3;
    
    public enum Type {
        SINGLE(RANK_SINGLE),
        MULTI(RANK_MULTI),
        ANY(RANK_ANY);
        
        private int matchingRank;
        
        private Type(int rank) {
            this.matchingRank = rank;
        }
        
        public int getRank() {
            return matchingRank;
        }
    }
    
    Type getMatcherType();
    int getMatchingRank();
    boolean matches(DeviceConfigurationIdentifier identifier);
    Serializable getMatcherIdentifier();
}
