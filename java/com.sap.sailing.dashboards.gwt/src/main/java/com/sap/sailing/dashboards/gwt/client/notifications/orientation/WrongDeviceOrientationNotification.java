package com.sap.sailing.dashboards.gwt.client.notifications.orientation;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.dashboards.gwt.client.device.Orientation;
import com.sap.sailing.dashboards.gwt.client.device.OrientationListener;
import com.sap.sailing.dashboards.gwt.client.device.OrientationType;
import com.sap.sse.common.Util.Pair;

public class WrongDeviceOrientationNotification extends AbsolutePanel implements OrientationListener {

    private Label rotateDeviceLabel;
    private AbsolutePanel notification;
    
    public WrongDeviceOrientationNotification() {
        super();
        WrongDeviceOrientationNotificationResources.INSTANCE.gss().ensureInjected();
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification_background());
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification_background_hidden());
        notification = new AbsolutePanel();
        notification.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification());
        notification.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().hidden());

        Image rotateDeviceIcon = new Image();
        rotateDeviceIcon.setResource(WrongDeviceOrientationNotificationResources.INSTANCE.rotatedevice());
        rotateDeviceIcon.getElement().addClassName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().logo());
        notification.add(rotateDeviceIcon);

        rotateDeviceLabel = new Label();
        rotateDeviceLabel.getElement().addClassName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().text());
        rotateDeviceLabel.setText("Rotate Device");
        notification.add(rotateDeviceLabel);
        
        this.add(notification);
        
        Orientation.getInstance().addListener(this);
        Orientation.getInstance().triggerDeviceOrientationRead();
    }

    public void show() {
        this.removeStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification_background_hidden());
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification_background_shown());
        notification.removeStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().hidden());
        notification.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().shown());
    }

    public void hide() {
        this.removeStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification_background_shown());
        this.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().notification_background_hidden());
        notification.removeStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().shown());
        notification.addStyleName(WrongDeviceOrientationNotificationResources.INSTANCE.gss().hidden());
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
