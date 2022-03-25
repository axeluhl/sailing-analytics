package com.sap.sse.landscape;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

/**
 * Equality and hash code are based on the {@link Named#getName() name} by default.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface AvailabilityZone extends Named, WithID {
    @Override
    String getId();
    
    Region getRegion();
}
