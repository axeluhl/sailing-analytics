package com.sap.sse.gwt.client.controls.busyindicator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class SimpleBusyIndicator extends BusyIndicator {
    
    private Image busyIndicator;
    
    /**
     * Creates a new SimpleBusyIndicator with the <code>busy</code> state <code>false</code>.<br />
     * The busy indicator component is a circling GIF.
     */
    public SimpleBusyIndicator() {
        this(false, 1.0f);
    }
    
    /**
     * Creates a new SimpleBusyIndicator with a custom <code>busy</code> state.<br />
     * The busy indicator component is a circling GIF.
     * 
     * @param busy Sets the busy state of the BusyIndicator
     * @param scale Scales the displayed image. 1.0 is 100%, 0.50 is 50%, ...
     */
    public SimpleBusyIndicator(boolean busy, float scale) {
        this(busy, scale, STYLE_NAME_PREFIX + "Simple", STYLE_NAME_PREFIX + "Circle");
    }
    
    public SimpleBusyIndicator(boolean busy, float scale, String panelCssClass, String imageCssClass) {
        this.setStyleName(panelCssClass);
        ImageResource resource = RESOURCES.busyIndicatorCircle();
        busyIndicator = new Image(resource.getSafeUri());
        busyIndicator.setStyleName(imageCssClass);
        busyIndicator.setPixelSize((int) (resource.getWidth() * scale), (int) (resource.getHeight() * scale));
        add(busyIndicator);
        setBusy(busy);
    }

    @Override
    public void setBusy(boolean busy) {
        super.setBusy(busy);
        busyIndicator.setVisible(busy);
    }
}
