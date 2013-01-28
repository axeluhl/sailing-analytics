package com.sap.sailing.domain.lifecycle;

/**
 * <p>When a state change occurs during a lifecycle a {@link LifecycleStateChangeListener} is triggered
 * and gets an instance of a {@link LifecycleStateChangedEvent}.</p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public interface LifecycleStateChangedEvent {
    
    public LifecycleState newState();
    public LifecycleState previousState();

}
