package com.sap.sailing.domain.common.tracking;

public interface WithValidityCache {
    boolean isValidityCached();
    
    /**
     * Returns a valid result if {@link #isValidCached()} returns <code>true</code>
     */
    boolean isValidCached();
    
    void invalidateCache();
    
    void cacheValidity(boolean isValid);
}
