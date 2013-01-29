package com.sap.sailing.domain.lifecycle.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.sap.sailing.domain.lifecycle.Lifecycle;
import com.sap.sailing.domain.lifecycle.LifecycleState;
import com.sap.sailing.domain.lifecycle.WithLifecycle;

/**
 * <p>Implementation of a lifecycle featuring properties saved to a HasMap.</p>a
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 29, 2013
 */
public abstract class LifecycleImpl implements Lifecycle {
    
    protected List<LifecycleState> history;
    protected LifecycleState state;
    
    private final WithLifecycle observer;
    private final Object monitor;
    
    public LifecycleImpl(WithLifecycle observer) {
        this.monitor = new Object[0];
        this.observer = observer;
        this.history = new ArrayList<LifecycleState>();
    }
        
    @Override
    public List<LifecycleState> getStateHistory() {
        return this.history;
    }

    @Override
    public LifecycleState getCurrentState() {
        return this.state;
    }

    @Override
    public void performTransitionTo(LifecycleState to) {
        synchronized(getMonitor()) {
            LifecycleState old = this.state;
            if (this.state == null) {
                old = to;
            }
            
            if (old.equals(to)) {
                /* Equal states, just update properties. We try to join non-existing
                 * ones and using "to" provided ones thus declaring new state as canonical.
                 * This will fail for null based properties. */
                for (Entry<String, Object> entry : to.allProperties().entrySet()) {
                      old.updateProperty(entry.getKey(), entry.getValue());
                }
            } else {
                this.history.add(this.state);
                this.state = to;
                
                /* first notify monitors and then the observer */
                this.getMonitor().notifyAll();
                if (this.getObserver() != null) {
                    this.getObserver().statusChanged(to, old);
                }
            }
        }
    }
    
    @Override
    public Object getMonitor() {
        return this.monitor;
    }
    
    @Override
    public WithLifecycle getObserver() {
        return this.observer;
    }

}
