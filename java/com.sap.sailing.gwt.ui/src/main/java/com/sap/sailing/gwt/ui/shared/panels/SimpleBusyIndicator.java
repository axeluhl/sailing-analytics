package com.sap.sailing.gwt.ui.shared.panels;

import com.google.gwt.user.client.ui.Image;

public class SimpleBusyIndicator extends BusyIndicator {
    
    private Image busyIndicator;
    
    /**
     * Creates a new SimpleBusyIndicator with the <code>busy</code> state <code>false</code>.<br />
     * The busy indicator component is a circling GIF.
     */
    public SimpleBusyIndicator() {
        this(false);
    }
    
    /**
     * Creates a new SimpleBusyIndicator with a custom <code>busy</code> state.<br />
     * The busy indicator component is a circling GIF.
     */
    public SimpleBusyIndicator(boolean busy) {
        busyIndicator = new Image(RESOURCES.busyIndicatorCircle());
        busyIndicator.setStyleName(STYLE_NAME_PREFIX + "busyIndicatorCircle");
        add(busyIndicator);
        setBusy(busy);
    }

    @Override
    public void setBusy(boolean busy) {
        this.busy = busy;
        busyIndicator.setVisible(busy);
//        Element elem = busyIndicator.getElement();
//        DOM.setStyleAttribute(elem, "visibility", busy ? "visible" : "hidden");
    }

}
