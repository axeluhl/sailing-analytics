package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class PlayEvent extends GwtEvent<PlayEvent.Handler> {
    
    public interface Handler extends EventHandler {
        void onStart(PlayEvent event);
    }
    
    private static final Type<Handler> type = new Type<>();
    
    public static com.google.gwt.event.shared.GwtEvent.Type<Handler> getType() {
        return type;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onStart(this);
    }
}
