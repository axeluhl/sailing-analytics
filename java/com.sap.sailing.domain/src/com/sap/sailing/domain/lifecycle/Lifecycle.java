package com.sap.sailing.domain.lifecycle;

import java.util.List;

/**
 * <p>Interface describing a lifecycle of an object. There are two possibilities
 * to be notified of lifecycle status changes: </p>
 * <ul>
 *  <li>First possibility is to register an Observer that gets notified. This Observer needs
 *      to implement the interface {@link WithLifecycle} and gets notified by {@link WithLifecycle#statusChanged(LifecycleState, LifecycleState)}.
 *      There is currently support for only one Observer as it is assumed that one {@link Lifecycle}
 *      instance belongs to only one object.
 *  </li>
 *  <li>
 *      Second possibility is to wait until {@link Lifecycle#getMonitor()} gets unblocked. This
 *      will occur at each state change. The new state can then be gathered by calling
 *      {@link Lifecycle#getCurrentState()} as the new state is set before waiting threads are
 *      unblocked. Old state can be determined by getting the last item of {@link Lifecycle#getStateHistory()}.
 *  </li>
 * </ul>
 * <p>To perform a state transition one has to call the {@link Lifecycle#performTransitionTo(LifecycleState)}
 * method. This method returns immediately after having notified Observer and waiting threads.</p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public interface Lifecycle {
    
    /**
     * @return List of {@link LifecycleState} this object has passed until now. The
     * list DOES NOT include the current state.
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
