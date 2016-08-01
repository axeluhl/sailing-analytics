package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

/**
 * This is used by the fix tracking mechanism to make fixes available in a track.
 * This makes it possible to e.g. map fixes to another type before adding them to the track.
 * <p>
 * 
 * Implementations need to be registered as OSGi service to be correctly found.
 *
 * @param <FixT> the type of fix this mapper handles
 * @param <TrackT> the type of track this mapper adds the fixes to
 * @param <K> the type of item the track is mapped to
 */
public interface SensorFixMapper<FixT extends Timed, TrackT extends DynamicTrack<FixT>, K extends WithID> {
    
    /**
     * Extracts the track from the TrackedRace or creates a new one if it doesn't already exist.
     * 
     * @param race the race to get the track for
     * @param key the mapped item to get the track for
     * @return the track to add fixes to
     */
    TrackT getTrack(DynamicTrackedRace race, K key);

    /**
     * This default implementation maps the given {@link DoubleVectorFix} using the {@link #map(DoubleVectorFix)} method
     * first. Afterwards, the mapped fix will be added to the given track.
     * 
     * @param track the track to add the fix to
     * @param fix the fix to add
     */
    default void addFix(TrackT track, DoubleVectorFix fix) {
        track.add(map(fix));
    }
    
    /**
     * This method maps the given {@link DoubleVectorFix} to the required fix type. 
     * 
     * @param fix the {@link DoubleVectorFix} to map
     * @return the mapped fix type instance
     */
    FixT map(DoubleVectorFix fix);
    
    /**
     * Checks if this mapper can be used for tracks/fixes that are associated to a given RegattaLog device mapping event.
     * 
     * @param eventType the type of the RegattaLog device mapping event
     * @return <code>true</code> if this mapper can handle fixes for the event, <code>false</code> otherwise.
     */
    boolean isResponsibleFor(Class<? extends RegattaLogDeviceMappingEvent<?>> eventType);
}
