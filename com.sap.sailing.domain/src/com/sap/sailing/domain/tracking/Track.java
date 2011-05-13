package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;

/**
 * A track records the {@link GPSFix}es received for an object of type <code>T</code>.
 * It allows clients to ask for a position at any given {@link TimePoint} and interpolates
 * the fixed positions to obtain an estimate of the position at the time requested.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <ItemType>
 */
public interface Track<ItemType, FixType extends GPSFix> {
    ItemType getTrackedItem();
    Iterable<FixType> getFixes();
    FixType getLastFixAtOrBefore(TimePoint timePoint);
    FixType getFirstFixAtOrAfter(TimePoint timePoint);
    FixType getLastFixBefore(TimePoint timePoint);
    FixType getFirstFixAfter(TimePoint timePoint);
    Position getEstimatedPosition(TimePoint timePoint);
}
