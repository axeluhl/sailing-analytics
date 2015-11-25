package com.sap.sailing.gwt.home.shared.refresh;

import com.sap.sailing.gwt.home.communication.SailingAction;

public interface ActionProvider<A extends SailingAction<?>> {
    
    A getAction();
    
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
