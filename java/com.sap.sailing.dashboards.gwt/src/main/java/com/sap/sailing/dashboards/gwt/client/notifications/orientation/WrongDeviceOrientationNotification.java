package com.sap.sailing.dashboards.gwt.client.notifications.orientation;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.dashboards.gwt.client.device.Orientation;
import com.sap.sailing.dashboards.gwt.client.device.OrientationListener;
import com.sap.sailing.dashboards.gwt.client.device.OrientationType;
import com.sap.sailing.dashboards.gwt.client.visualeffects.BlurEffect;
import com.sap.sse.common.Util.Pair;

public class WrongDeviceOrientationNotification extends AbsolutePanel implements OrientationListener {

    private Label rotateDeviceLabel;
    
    public WrongDeviceOrientationNotification() {
        super();
        WrongDeviceOrientationNotificationResources.INSTANCE.gss().ensureInjected();
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification());
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().hidden());

        Image rotateDeviceIcon = new Image();
        rotateDeviceIcon.setResource(WrongDeviceOrientationNotificationResources.INSTANCE.rotatedevice());
        rotateDeviceIcon.getElement().addClassName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().logo());
        add(rotateDeviceIcon);

        rotateDeviceLabel = new Label();
        rotateDeviceLabel.getElement().addClassName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().text());
        rotateDeviceLabel.setText("Rotate Device");
        add(rotateDeviceLabel);
        
        Orientation.getInstance().addListener(this);
        Orientation.getInstance().triggerDeviceOrientationRead();
    }

    public void show() {
        this.removeStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().hidden());
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().shown());
        BlurEffect.getInstance().addToView(RootLayoutPanel.get());
    }

    public void hide() {
        this.removeStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().shown());
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().hidden());
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
