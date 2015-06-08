package com.sap.sailing.domain.tracking;

import java.util.Set;

import com.sap.sailing.domain.base.Regatta;

/**
 * Sometimes {@link TrackedRace}s want to know which {@link TrackedRace} is in front of them in the execution order
 * of a {@link Regatta}. This is i.e interesting to know in the {@link DynamicTrackedRace #recordWind(com.sap.sailing.domain.common.Wind, com.sap.sailing.domain.common.WindSource)} 
 * method, when the race has to decide whether to record the wind. If a previous {@link TrackedRace} 
 * is <code>null</code>, the current {@link TrackedRace} would save the wind fixes earlier.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public interface RaceExecutionOrderProvider {

    /**
     * Parameter <code>race</code> is a {@link TrackedRace} from which the method returns the previous
     * {@link TrackedRace}s in the execution order of a {@link Regatta}. This expresses the fact that the races returned
     * by this method must all have been finished before <code>race</code> can start.
     */
    Set<TrackedRace> getPreviousRaceInExecutionOrder(TrackedRace race);

    /**
     * When the owner of this cache-like provider has initialized all its structures that this cache requires
     * it shall call this method. This applies in particular after de-serializing the owner because during de-serialization,
     * even during the <code>readObject</code> method, the owner's fields may not all be initialized. The
     * <code>readResolve()</code> method is usually a good place to do so.
     */
    void triggerUpdate();
}
