package com.sap.sailing.domain.tractracadapter.impl;

import java.util.concurrent.LinkedBlockingQueue;

import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.util.Util.Triple;

/**
 * Some event receiver that can be executed in a thread because it's a runnable, and
 * manages a queue of events received. The events are expected to be triplets.
 * 
 * @author Axel Uhl (d043530)
 */
public abstract class AbstractReceiverWithQueue<A, B, C> implements Runnable, Receiver {
    private final LinkedBlockingQueue<Triple<A, B, C>> queue;

    public AbstractReceiverWithQueue() {
        super();
        this.queue = new LinkedBlockingQueue<Triple<A, B, C>>();
    }
    
    public void stop() {
        // mark the end and hence terminate the thread by adding a null/null/null event to the queue
        queue.clear();
        queue.add(new Triple<A, B, C>(null, null, null));
    }
    
    protected void enqueue(Triple<A, B, C> event) {
        queue.add(event);
    }
    
    private boolean isStopEvent(Triple<A, B, C> event) {
        return event.getA() == null && event.getB() == null && event.getC() == null;
    }

    @Override
    public void run() {
        Triple<A, B, C> event = null;
        while (event == null || !isStopEvent(event)) {
            try {
                event = queue.take();
                if (!isStopEvent(event)) {
                    handleEvent(event);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void handleEvent(Triple<A, B, C> event);

}
