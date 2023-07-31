package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Distance;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithID;

/**
 * A named area in a sailing {@link Venue} where races take place. Usually, the areas carry names according to the NATO
 * alphabet, such as "Alpha", "Bravo" or "Charly".<p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface CourseArea extends NamedWithID, IsManagedByCache<SharedDomainFactory<?>> {
    UUID getId();
    
    /**
     * An optional position specifying the center of a course area assumed to be of circular shape.
     * If {@code null}, the position is not known.
     */
    Position getCenterPosition();
    
    void setCenterPosition(Position centerPosition);
    
    /**
     * If {@link #getCenterPosition()} delivers a non-{@code null} result, asking the radius of this course area, which
     * is assumed to be of circulare shape, can make sense. If may, however, not be defined in which case {@code null}
     * is returned.
     */
    Distance getRadius();

    void setRadius(Distance radius);
}
