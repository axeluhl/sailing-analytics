package com.sap.sailing.domain.tracking;

public interface WithValidityCache {
    boolean isValidityCached();
    
    boolean isValid();
    
    void invalidateCache();
    
    void cacheValidity(boolean isValid);
}
