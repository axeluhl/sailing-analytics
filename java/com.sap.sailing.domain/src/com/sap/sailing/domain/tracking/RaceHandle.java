package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;

/**
 * Gives clients a way to refer to a specific set of races even though the {@link RaceDefinition} objects may not have
 * been created yet, but without exposing the tracking provider's internals. The {@link #getRace()} call will block
 * until the {@link RaceDefinition}s have been created.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceHandle {

    Regatta getRegatta();
    
    /**
     * Fetch the race definitions. If the race definition represented by this handle hasn't been created yet, the call
     * blocks until such a definition is provided by another call, usually by the {@link RaceCourseReceiver}.
     */
    RaceDefinition getRace();

    /**
     * Fetch the race definition. If the race definition represented by this handle hasn't been created yet, the call
     * blocks until such a definition is provided by another call, usually by the {@link RaceCourseReceiver}. If
     * <code>timeoutInMilliseconds</code> milliseconds have passed and the race definition is found not to have
     * shown up until then, a valid but empty set is returned. The unblocking may be deferred even beyond
     * <code>timeoutInMilliseconds</code> in case no modifications happen on the {@link DomainFactory}'s
     * set of races during that time.
     */
    RaceDefinition getRace(long timeoutInMilliseconds);

    DynamicTrackedRegatta getTrackedRegatta();

    /**
     * The tracker managing the tracking of the race to which this is a handle.
     */
    RaceTracker getRaceTracker();

}
