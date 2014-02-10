package com.sap.sailing.domain.common;

/**
 * A time duration that can be converted to various time units and that interoperates with {@link TimePoint}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Duration {
    long asMillis();
    
    Duration divide(long divisor);
}
