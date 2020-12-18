package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sse.util.WeakReferenceWithCleanerCallback;

/**
 * A listener that unregisters itself from the race log's listeners if its notification target is no longer strongly
 * referenced. Note that the visitor is <em>not</em> automatically registered by the constructor although the
 * {@link RaceLog} object is passed in.
 * {@link RaceLog#addListener(com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor) registration} of this visitor
 * as a listener on the {@link RaceLog} has to happen explicitly by the client.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class WeakRaceLogChangedVisitor extends AbstractRaceLogChangedVisitor {
    private final WeakReferenceWithCleanerCallback<RaceLogChangedListener> listenerRef;
    
    /**
     * Remembers from which {@link RaceLog} to {@link RaceLog#removeListener(Object) remove} this visitor as a listener
     * when the <code>listener</code> is no longer strongly referenced. This constructor does <em>not</em> register this
     * visitor as a listener on the {@link RaceLog}. This has to happen explicitly by the caller after this constructor
     * has returned.
     */
    public WeakRaceLogChangedVisitor(RaceLog removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced, RaceLogChangedListener listener) {
        listenerRef = new WeakReferenceWithCleanerCallback<>(listener, ()->removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced.removeListener(this));
    }
    
    @Override
    protected void notifyListenerAboutEventAdded(RaceLogEvent event) {
        RaceLogChangedListener listener = listenerRef.get();
        if (listener != null) {
            listener.eventAdded(event);
        }
    }
}
