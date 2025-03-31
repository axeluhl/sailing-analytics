package com.sap.sailing.dashboards.gwt.client.notifications.orientation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface WrongDeviceOrientationNotificationResources extends ClientBundle {

    public static final WrongDeviceOrientationNotificationResources INSTANCE =  GWT.create(WrongDeviceOrientationNotificationResources.class);
    
    @Source("com/sap/sailing/dashboards/gwt/client/images/rotatedevice.png")
    ImageResource rotatedevice();
    
    @Source({ "com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "WrongDeviceOrientationNotification.gss"})
    WrongDeviceNotificationGSS gss();

    public interface WrongDeviceNotificationGSS extends CssResource {
        public String notification_background();
        public String notification_background_shown();
        public String notification_background_hidden();
        public String notification();
        public String shown();
        public String hidden();
        public String logo();
        public String text();
    }
}
