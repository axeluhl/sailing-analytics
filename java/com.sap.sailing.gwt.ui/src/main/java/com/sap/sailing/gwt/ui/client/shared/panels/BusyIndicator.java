package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A widget to show if something is currently busy.
 * @author Lennart Hensler (D054527)
 */
public abstract class BusyIndicator extends FlowPanel {
    
    protected final static String STYLE_NAME_PREFIX = "busyIndicator-";
    protected final static SharedPanelsResources RESOURCES = GWT.create(SharedPanelsResources.class);
    
    protected boolean busy;
    
    /**
     * Sets the <code>busy</code> state of the BusyIndicator, which displays or hides the busy indicator.
     * @param busy The new <code>busy</code> state
     */
    public abstract void setBusy(boolean busy);
    
    public boolean isBusy() {
        return busy;
    }

}
