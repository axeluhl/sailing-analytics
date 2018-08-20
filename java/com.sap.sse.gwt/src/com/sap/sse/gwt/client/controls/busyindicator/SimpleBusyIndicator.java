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
        this(false, 1.0f, RESOURCES.busyIndicatorCircle());
    }

    public SimpleBusyIndicator(ImageResource imageResource) {
        this(false, 1.0f, imageResource);
    }

    /**
     * Creates a new SimpleBusyIndicator with a custom <code>busy</code> state.<br />
     * The busy indicator component is a circling GIF.
     * 
     * @param busy Sets the busy state of the BusyIndicator
     * @param scale Scales the displayed image. 1.0 is 100%, 0.50 is 50%, ...
     */
    public SimpleBusyIndicator(boolean busy, float scale) {
        this(busy, scale, RESOURCES.busyIndicatorCircle());
    }

    public SimpleBusyIndicator(boolean busy, float scale, ImageResource imageResource) {
        this.setStyleName(STYLE_NAME_PREFIX + "Simple");
        busyIndicator = new Image(imageResource.getSafeUri());
        busyIndicator.setStyleName(STYLE_NAME_PREFIX + "Circle");
        busyIndicator.setPixelSize((int) (imageResource.getWidth() * scale), (int) (imageResource.getHeight() * scale));
        add(busyIndicator);
        setBusy(busy);
    }

    public void setPanelStyleClass(String panelCssClass) {
        this.setStyleName(panelCssClass);
    }

    public void setImageStyleClass(String imageCssClass) {
        busyIndicator.setStyleName(imageCssClass);
    }

    @Override
    public void setBusy(boolean busy) {
        super.setBusy(busy);
        busyIndicator.setVisible(busy);
    }
}
