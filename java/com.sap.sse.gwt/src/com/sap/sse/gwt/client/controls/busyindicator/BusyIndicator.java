package com.sap.sse.gwt.client.controls.busyindicator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A widget to show if something is currently busy.
 * @author Lennart Hensler (D054527)
 */
public abstract class BusyIndicator extends FlowPanel implements BusyDisplay {
    
    protected final static String STYLE_NAME_PREFIX = "busyIndicator-";
    protected final static BusyIndicatorResources RESOURCES = GWT.create(BusyIndicatorResources.class);
    
    private boolean busy;
    
    /**
     * Sets the <code>busy</code> state of the BusyIndicator, which displays or hides the busy indicator.
     * @param busy The new <code>busy</code> state
     */
    @Override
    public void setBusy(boolean busy) {
        this.busy = busy;
    }
    
    public boolean isBusy() {
        return busy;
    }
}
