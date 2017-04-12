package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

/**
 * Trigger "in between/ idle mode" path in autoplay
 */
public class AutoPlayNodeTransitionRequestEvent extends GwtEvent<AutoPlayNodeTransitionRequestEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private AutoPlayNode nodeToTransitionTo;

    public AutoPlayNodeTransitionRequestEvent(AutoPlayNode nodeToTransitionTo) {
        this.nodeToTransitionTo = nodeToTransitionTo;
    }

    public AutoPlayNode getNodeToTransitionTo() {
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
