package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

/**
 * Trigger "in between/ idle mode" path in autoplay
 */
public class FallbackToIdleNodePathEvent extends GwtEvent<FallbackToIdleNodePathEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private final AutoPlayNode source;

    public FallbackToIdleNodePathEvent(AutoPlayNode source) {
        this.source = source;
    }

    public AutoPlayNode getSource() {
        return source;
    }

    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void onFallbackToIdle(FallbackToIdleNodePathEvent e);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onFallbackToIdle(this);
    }
}
