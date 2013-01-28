package com.sap.sailing.domain.lifecycle;

import java.util.List;

/**
 * <p>Interface describing a lifecycle of an object</p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public interface Lifecycle {
    
    /**
     * @return List of {@link LifecycleState} this object has passed until now. The
     * list also includes the current state.
     */
    public List<LifecycleState> getStateHistory();
    
    /**
     * @return The current state as instance of {@link LifecycleState}
     */
    public LifecycleState getCurrentState();
    
    /**
     * Executes the transition to the given state. No checks will be performed
     * if this state is valid.
     * 
     * @param to The new state for the object
     */
    public void performTransitionTo(LifecycleState to);
    
    /**
     * @return The object that can be waited for and gets notified whenever a transition change is triggered.
     */
    public Object getMonitor();
    
    /**
     * @return The object that will be notified of changes
     */
    public WithLifecycle getObserver();
    
}
