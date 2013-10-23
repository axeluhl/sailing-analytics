package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

public interface DeviceConfigurationMatcher extends Serializable {
    public final static int RANK_SINGLE = 1;
    public final static int RANK_MULTI = 2;
    public final static int RANK_ANY = 3;
    
    int getMatchingRank();
    boolean matches(DeviceConfigurationIdentifier identifier);
}
