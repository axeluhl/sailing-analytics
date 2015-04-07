package com.sap.sailing.dashboards.gwt.client.notifications;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.dashboards.gwt.client.RibDashboardImageResources;
import com.sap.sailing.dashboards.gwt.client.device.Orientation;
import com.sap.sailing.dashboards.gwt.client.device.OrientationListener;
import com.sap.sailing.dashboards.gwt.client.device.OrientationType;
import com.sap.sailing.dashboards.gwt.client.visualeffects.BlurEffect;
import com.sap.sse.common.Util.Pair;

public class WrongDeviceOrientationNotification extends AbsolutePanel implements OrientationListener {

    private Label rotateDeviceLabel;
    
    public WrongDeviceOrientationNotification() {
        super();
        this.addStyleName("wrongOrientationNotification");
        this.addStyleName("wrongOrientationNotification_hidden");

        Image rotateDeviceIcon = new Image();
        rotateDeviceIcon.setResource(RibDashboardImageResources.INSTANCE.rotatedevice());
        rotateDeviceIcon.getElement().addClassName("wrongOrientationNotification_logo");
        add(rotateDeviceIcon);

        rotateDeviceLabel = new Label();
        rotateDeviceLabel.getElement().addClassName("wrongOrientationNotification_text");
        rotateDeviceLabel.setText("Rotate Device");
        add(rotateDeviceLabel);
        
        Orientation.getInstance().addListener(this);
        Orientation.getInstance().triggerDeviceOrientationRead();
    }

    public void show() {
        this.removeStyleName("wrongOrientationNotification_hidden");
        this.addStyleName("wrongOrientationNotification_shown");
        BlurEffect.getInstance().addToView(RootLayoutPanel.get());
    }

    public void hide() {
        this.removeStyleName("wrongOrientationNotification_shown");
        this.addStyleName("wrongOrientationNotification_hidden");
        BlurEffect.getInstance().removeFromView(RootLayoutPanel.get());
    }

    @Override
    public void orientationChanged(Pair<OrientationType, Double> orientation) {
        OrientationType orientationType = orientation.getA();
        switch (orientationType) {
        case PORTRAIT_UP:
            show();
            break;
        case PORTRAIT_DOWN:
            show();
            break;
        case LANDSCAPE_RIGHT:
            hide();
            break;
        case LANDSCAPE_LEFT:
            hide();
            break;
        }
    }
}
