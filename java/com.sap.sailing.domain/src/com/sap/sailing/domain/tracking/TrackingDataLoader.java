package com.sap.sailing.domain.tracking;

/**
 * Marker interface for classes that load data for a {@link TrackedRace} and therefore contribute to the composite
 * status of a tracked race. A {@link TrackingDataLoader} must update it's state in the associated {@link TrackedRace}
 * by calling {@link DynamicTrackedRace#onStatusChanged(TrackingDataLoader, TrackedRaceStatus)} with a self-reference
 * and the actual status.
 */
public interface TrackingDataLoader {
}
