package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sse.common.WithID;

/**
 * Mutable/dynamic version of {@link SensorFixTrack}.
 *
 * @param <ItemType> the type of item this track is mapped to
 * @param <FixT> the type of fix that is contained in this track
 */
public interface DynamicSensorFixTrack<ItemType extends WithID & Serializable, FixT extends SensorFix> extends
        SensorFixTrack<ItemType, FixT>, DynamicTrack<FixT> {
    /**
     * Invoked when this track is added to a tracked race. The method is also invoked after de-serializing the
     * containing {@link TrackedRace}. This default implementation does nothing.
     */
    default void addedToTrackedRace(TrackedRace trackedRace) {}
}
