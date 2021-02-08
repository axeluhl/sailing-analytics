package com.sap.sailing.gwt.managementconsole.events.regatta;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaListUpdateEvent extends GwtEvent<RegattaListUpdateEvent.Handler> {

    public interface Handler extends EventHandler {
        void onRegattaUpdate(RegattaListUpdateEvent event);
    }

    public static Type<Handler> TYPE = new Type<>();

    private final List<RegattaDTO> regattas;

    public RegattaListUpdateEvent(List<RegattaDTO> regattas) {
        this.regattas = regattas;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onRegattaUpdate(this);
    }

    public List<RegattaDTO> getRegattas() {
        return regattas;
    }

}
