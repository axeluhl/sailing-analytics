package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.Date;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to be fired by {@link AbstractRaceChart} when a user zooms into the chart.
 */
public class ChartZoomChangedEvent extends GwtEvent<ChartZoomChangedEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<>();
    private final Date rangeStart;
    private final Date rangeEnd;

    public interface Handler extends EventHandler {
        void handleZoomChanged(ChartZoomChangedEvent cze);
    }

    public ChartZoomChangedEvent(Date rangeStart, Date rangeEnd) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public Date getRangeStart() {
        return rangeStart;
    }

    public Date getRangeEnd() {
        return rangeEnd;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(Handler handler) {
        handler.handleZoomChanged(this);
    }
}
