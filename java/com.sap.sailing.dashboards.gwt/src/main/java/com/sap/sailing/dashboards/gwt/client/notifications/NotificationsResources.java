package com.sap.sailing.dashboards.gwt.client.notifications;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface NotificationsResources extends ClientBundle {

    @Source("Notifications.css")
    NotificationsStyle notificationsStyle();
    
    public interface NotificationsStyle extends CssResource {
        public String bottomnotification();
        public String bottomnotification_shown();
        public String bottomnotification_hidden();
        public String wrongorientationnotification();
        public String wrongorientationnotification_shown();
        public String wrongorientationnotification_hidden();
        public String wrongorientationnotification_logo();
        public String wrongorientationnotification_text();
    }
}
