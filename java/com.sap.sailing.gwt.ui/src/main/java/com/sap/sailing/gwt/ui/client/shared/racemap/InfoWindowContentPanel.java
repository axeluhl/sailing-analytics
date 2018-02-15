package com.sap.sailing.gwt.ui.client.shared.racemap;


import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.user.client.ui.SimplePanel;

public class InfoWindowContentPanel extends SimplePanel {
    private boolean attached = false;

    public InfoWindowContentPanel(InfoWindow infoWindowToSetTo) {
        infoWindowToSetTo.setContent(this);
        final Collection<HandlerRegistration> handlerRegistrations = new HashSet<>();
        handlerRegistrations.add(infoWindowToSetTo.addDomReadyHandler(event -> maybeAttach()));
        handlerRegistrations.add(infoWindowToSetTo.addCloseClickHandler(event -> maybeDetach()));
        handlerRegistrations.add(infoWindowToSetTo.addContentChangeHandler(event -> {
            handlerRegistrations.forEach(HandlerRegistration::removeHandler);
            maybeDetach();
        }));
    }
    
    private void maybeAttach() {
        if (!attached) {
            attached = true;
            onAttach();
            GWT.log("Attaching info window");
        }
    }
    
    private void maybeDetach() {
        if (attached) {
            attached = false;
            onDetach();
            GWT.log("Detaching info window");
        }
    }
}
