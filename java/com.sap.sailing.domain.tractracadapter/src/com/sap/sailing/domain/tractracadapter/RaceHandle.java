package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tractracadapter.impl.RaceCourseReceiver;

/**
 * Gives clients a way to refer to a specific race even though the {@link RaceDefinition} object may not have
 * been created yet, but without exposing the TracTrac internals. The {@link #getRace()} call will block
 * until the {@link RaceDefinition} has been created.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceHandle {

    Event getEvent();
    
    /**
     * Fetch the race definition. If the race definition represented by this handle hasn't been created yet, the call
     * blocks until such a definition is provided by another call, usually by the {@link RaceCourseReceiver}.
     */
    RaceDefinition getRace();

    /**
     * Fetch the race definition. If the race definition represented by this handle hasn't been created yet, the call
     * blocks until such a definition is provided by another call, usually by the {@link RaceCourseReceiver}. If
     * <code>timeoutInMilliseconds</code> milliseconds have passed and the race definition is found not to have
     * shown up until then, <code>null</code> is returned. The unblocking may be deferred even beyond
     * <code>timeoutInMilliseconds</code> in case no modifications happen on the {@link DomainFactory}'s
     * set of races during that time.
     */
    RaceDefinition getRace(long timeoutInMilliseconds);

    DynamicTrackedEvent getTrackedEvent();

    /**
     * The tracker managing the tracking of the race to which this is a handle.
     */
    RaceTracker getRaceTracker();

}
