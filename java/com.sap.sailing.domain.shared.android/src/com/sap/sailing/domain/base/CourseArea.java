package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithID;

/**
 * A named area in a sailing {@link Venue} where races take place. Usually, the areas carry names according to the NATO
 * alphabet, such as "Alpha", "Bravo" or "Charly".
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CourseArea extends NamedWithID, IsManagedByCache<SharedDomainFactory> {
    UUID getId();
}
