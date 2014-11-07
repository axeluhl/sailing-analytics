package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sailing.domain.common.WithID;
import com.sap.sse.common.Named;

/**
 * A named area in a sailing {@link Venue} where races take place. Usually, the areas carry names according to the NATO
 * alphabet, such as "Alpha", "Bravo" or "Charlie".
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CourseArea extends Named, WithID, IsManagedByCache {
    UUID getId();
}
