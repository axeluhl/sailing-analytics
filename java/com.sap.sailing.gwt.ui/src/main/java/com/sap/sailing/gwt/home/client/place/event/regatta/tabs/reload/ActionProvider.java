package com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;

public interface ActionProvider<A extends Action<?>> {
    
    A getAction();

    public class DefaultActionProvider<A extends Action<?>> implements ActionProvider<A> {
        private final A action;

        public DefaultActionProvider(A action) {
            this.action = action;
        }
        
        @Override
        public A getAction() {
            return action;
        }
    }
}
