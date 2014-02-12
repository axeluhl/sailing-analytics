package com.sap.sailing.domain.common;

import java.io.Serializable;

/**
 * A time duration that can be converted to various time units and that interoperates with {@link TimePoint}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Duration extends Serializable {
    long asMillis();
    
    double asSeconds();
    
    Duration divide(long divisor);
}
