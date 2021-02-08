package com.sap.sailing.gwt.managementconsole.events.regatta;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class RegattaListRequestEvent extends GwtEvent<RegattaListRequestEvent.Handler> {

    public interface Handler extends EventHandler {
        void onRegattaListResponse(RegattaListRequestEvent event);
    }

    public static Type<Handler> TYPE = new Type<>();

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onRegattaListResponse(this);
    }

}
