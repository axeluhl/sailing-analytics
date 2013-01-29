package com.sap.sailing.domain.lifecycle;

import java.util.EventListener;

import com.sap.sailing.domain.common.LifecycleState;

/**
 * <p>During a lifecycle of an object many state changes can occur (see {@link LifecycleState}). These changes
 * can trigger different actions. To achieve this goal one can register for such state changes and perform
 * the appropriate actions</p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public interface LifecycleStateChangeListener extends EventListener {

    public void transitionFired(LifecycleStateChangedEvent e);
}
