package com.sap.sailing.domain.abstractlog.race.impl;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

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
    private static final Logger logger = Logger.getLogger(WeakRaceLogChangedVisitor.class.getName());
    private static ReferenceQueue<? super RaceLogChangedListener> queue = new ReferenceQueue<RaceLogChangedListener>();
    private static Map<Reference<?>, WeakRaceLogChangedVisitor> referenceToVisitor = new ConcurrentHashMap<Reference<?>, WeakRaceLogChangedVisitor>();
    static {
        new Thread(WeakRaceLogChangedVisitor.class.getSimpleName() + " weak reference cleaner") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Reference<?> ref = queue.remove();
                        WeakRaceLogChangedVisitor visitor = referenceToVisitor.remove(ref);
                        visitor.removeListener();
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Error trying to clean weak reference", e);
                    } catch (Exception e2) {
                        logger.severe("Shouldn't have gotten here. Continuing...");
                    }
                }
            }
        }.start();
    }
    
    private final WeakReference<RaceLogChangedListener> listenerRef;
    private final RaceLog removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced;
    
    /**
     * Remembers from which {@link RaceLog} to {@link RaceLog#removeListener(Object) remove} this visitor as a listener
     * when the <code>listener</code> is no longer strongly referenced. This constructor does <em>not</em> register this
     * visitor as a listener on the {@link RaceLog}. This has to happen explicitly by the caller after this constructor
     * has returned.
     */
    public WeakRaceLogChangedVisitor(RaceLog removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced, RaceLogChangedListener listener) {
        this.removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced = removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced;
        listenerRef = new WeakReference<RaceLogChangedListener>(listener, queue);
        referenceToVisitor.put(listenerRef, this);
    }
    
    @Override
    protected void notifyListenerAboutEventAdded(RaceLogEvent event) {
        RaceLogChangedListener listener = listenerRef.get();
        if (listener == null) {
            removeListener();
        } else {
            listener.eventAdded(event);
        }
    }

    private void removeListener() {
        removeFromThisRaceLogWhenListenerNoLongerStronglyReferenced.removeListener(this);
    }
}
