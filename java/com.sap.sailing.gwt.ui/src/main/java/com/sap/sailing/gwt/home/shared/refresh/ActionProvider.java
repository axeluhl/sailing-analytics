package com.sap.sailing.gwt.home.shared.refresh;

import com.sap.sailing.gwt.home.communication.SailingAction;

/**
 * Provides a SailingAction instance that is used by {@link RefreshManager} to load data consumed by a
 * {@link RefreshableWidget}.SailingAction
 *
 * @param <A>
 *            The type of action given by the {@link ActionProvider}.
 */
public interface ActionProvider<A extends SailingAction<?>> {
    
    A getAction();
    
    /**
     * @return true if this ActionProvider is active and a refresh may occur, false otherwise.
     */
    boolean isActive();
    
    public abstract class AbstractActionProvider<A extends SailingAction<?>> implements ActionProvider<A> {
        private final A action;
        
        public AbstractActionProvider(A action) {
            this.action = action;
        }
        
        @Override
        public A getAction() {
            return action;
        }
    }

    /**
     * Default implementation of {@link ActionProvider} that returns a non-changing action instance.
     * 
     * @param <A>
     *            The type of action given by the {@link ActionProvider}.
     */
    public class DefaultActionProvider<A extends SailingAction<?>> extends AbstractActionProvider<A> {

        public DefaultActionProvider(A action) {
            super(action);
        }
        
        @Override
        public boolean isActive() {
            return true;
        }
    }
}
