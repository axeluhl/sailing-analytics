package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event thrown after the place changed.
 */
public class SlideHeaderEvent extends GwtEvent<SlideHeaderEvent.Handler> {

    /**
     * Implemented by handlers of PlaceChangedEvent.
     */
    public interface Handler extends EventHandler {
        void onHeaderChanged(SlideHeaderEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final String headerText;
    private final String headerSubText;

    public SlideHeaderEvent(String headerText, String headerSubText) {
        this.headerText = headerText;
        this.headerSubText = headerSubText;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    public String getHeaderText() {
        return headerText;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onHeaderChanged(this);
    }

    public String getHeaderSubText() {
        return headerSubText;
    }
}
