package com.sap.sailing.domain.lifecycle;

/**
 * <p>Interface all objects need to implement that have a lifecycle.
 * See {@link Lifecycle} for more information. </p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public interface WithLifecycle {
    
    public Lifecycle getLifecycle();
    
    public void statusChanged(LifecycleState newState, LifecycleState oldState);

}
