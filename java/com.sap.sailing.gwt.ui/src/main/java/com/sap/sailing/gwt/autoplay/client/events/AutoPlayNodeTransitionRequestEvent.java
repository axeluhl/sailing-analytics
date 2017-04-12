package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

/**
 * Trigger "in between/ idle mode" path in autoplay
 */
public class AutoPlayNodeTransitionRequestEvent extends GwtEvent<AutoPlayNodeTransitionRequestEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private AutoPlayNodeController nodeToTransitionTo;

    public AutoPlayNodeTransitionRequestEvent(AutoPlayNodeController nodeToTransitionTo) {
        this.nodeToTransitionTo = nodeToTransitionTo;
    }

    public AutoPlayNodeController getNodeToTransitionTo() {
        return nodeToTransitionTo;
    }

    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void onAutoPlayNodeTransition(AutoPlayNodeTransitionRequestEvent e);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onAutoPlayNodeTransition(this);
    }
}
