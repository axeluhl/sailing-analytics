package com.sap.sse.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Use together with {@link Triggerable}. When {@link #get accessing} the trigger it triggers all
 * {@link Triggerable}s {@link #register registered} with it. For {@link Triggerable}s not yet
 * executed, this will run the {@link Triggerable}'s behavior. In any case, all {@link Triggerable}s
 * will be unregistered at that point.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class Trigger<T> {
    private final T t;
    
    /**
     * {@code null} if no {@link Triggerable} is currently registered
     */
    private List<Triggerable> triggerables;
    
    public Trigger(T t) {
        super();
        this.t = t;
    }
    
    public synchronized void register(Triggerable triggerable) {
        if (triggerables == null) {
            triggerables = new ArrayList<>();
        }
        triggerables.add(triggerable);
    }

    public synchronized T get() {
        if (triggerables != null) {
            final List<Triggerable> copy = triggerables;
            triggerables = null;
            for (final Triggerable triggerable : copy) {
                triggerable.run();
            }
        }
        return t;
    }
}
