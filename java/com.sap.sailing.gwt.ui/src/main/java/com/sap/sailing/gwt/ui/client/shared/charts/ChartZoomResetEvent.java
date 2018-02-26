package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to be fired by {@link AbstractRaceChart} when a user resets the zoom to show the whole time range again.
 */
public class ChartZoomResetEvent extends GwtEvent<ChartZoomResetEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<>();

    public interface Handler extends EventHandler {
        void handleZoomReset(ChartZoomResetEvent czre);
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(Handler handler) {
        handler.handleZoomReset(this);
    }
}
