package com.sap.sse.gwt.client;

import com.google.gwt.user.client.Timer;
import com.sap.sse.common.util.Triggerable;

/**
 * A {@link Timer} that {@link Triggerable#run() runs} a {@link Triggerable} when firing. In particular, if the
 * {@link Triggerable} at that point has already been executed once before then it will not execute again when
 * the timer fires.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TriggerableTimer extends Timer {
    
    private final Triggerable triggerable;

    public TriggerableTimer(Triggerable triggerable) {
        this.triggerable = triggerable;
    }

    @Override
    public void run() {
        triggerable.run();
    }

}
