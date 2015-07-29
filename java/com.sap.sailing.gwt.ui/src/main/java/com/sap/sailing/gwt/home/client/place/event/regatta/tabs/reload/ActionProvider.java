package com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;

public interface ActionProvider<A extends Action<?>> {
    
    A getAction();
    
    boolean isActive();
    
    public abstract class AbstractActionProvider<A extends Action<?>> implements ActionProvider<A> {
        private final A action;
        
        public AbstractActionProvider(A action) {
            this.action = action;
        }
        
        @Override
        public A getAction() {
            return action;
        }
    }

    public class DefaultActionProvider<A extends Action<?>> extends AbstractActionProvider<A> {

        public DefaultActionProvider(A action) {
            super(action);
        }
        
        @Override
        public boolean isActive() {
            return true;
        }
    }
}
