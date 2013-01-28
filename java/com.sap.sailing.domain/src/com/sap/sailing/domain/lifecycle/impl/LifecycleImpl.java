package com.sap.sailing.domain.lifecycle.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.lifecycle.Lifecycle;
import com.sap.sailing.domain.lifecycle.LifecycleState;
import com.sap.sailing.domain.lifecycle.WithLifecycle;

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
            
            this.history.add(this.state);
            this.state = to;
            
            /* first notify monitors and then the observer */
            this.getMonitor().notifyAll();
            this.getObserver().statusChanged(to, old);
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
