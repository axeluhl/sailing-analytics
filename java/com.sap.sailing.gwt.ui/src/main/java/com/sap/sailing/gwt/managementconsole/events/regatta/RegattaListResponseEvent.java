package com.sap.sailing.gwt.managementconsole.events.regatta;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaListResponseEvent extends GwtEvent<RegattaListResponseEvent.Handler> {

    public interface Handler extends EventHandler {
        void onRegattasRefreshed(RegattaListResponseEvent event);
    }

    public static Type<RegattaListResponseEvent.Handler> TYPE = new Type<>();

    private final List<RegattaDTO> regattas;

    public RegattaListResponseEvent(final List<RegattaDTO> regattas) {
        this.regattas = regattas;
    }

    @Override
    public Type<RegattaListResponseEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RegattaListResponseEvent.Handler handler) {
        handler.onRegattasRefreshed(this);
    }

    public List<RegattaDTO> getRegattas() {
        return regattas;
    }

}
