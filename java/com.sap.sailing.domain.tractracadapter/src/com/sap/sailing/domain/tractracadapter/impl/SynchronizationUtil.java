package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;

/**
 * Since the switch from TTCM to TracAPI, some calls seem to require synchronization to make them thread safe. A prominent
 * example is the call to {@link IEvent#getRaces()} which throws a {@link ConcurrentModificationException} in many non-obvious
 * cases.<p>
 * 
 * This helper class offers a few methods that can be used to obtain a clone of such a data structure in a synchronized way.
 * If, by convention, all the code in this connector adheres to the pattern of using this class instead of performing a direct
 * call (which is impossible to enforce, really) then we should be better off.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SynchronizationUtil {
    /**
     * Obtains a copy of the {@link IEvent#getRaces()} result while synchronizing on the result of calling
     * {@link IEvent#getRaces()}.
     */
    public static Iterable<IRace> getRaces(IEvent event) {
        List<IRace> races = event.getRaces();
        synchronized (races) {
            return new ArrayList<IRace>(races);
        }
    }
}
