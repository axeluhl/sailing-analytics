package com.sap.sailing.dashboards.gwt.client.notifications.bottom;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface BottomNotificationResources extends ClientBundle{

    public static final BottomNotificationResources INSTANCE =  GWT.create(BottomNotificationResources.class);
    
    @Source({"com/sap/sailing/dashboards/gwt/client/resources/theme/theme.gss", "BottomNotification.gss"})
    NotificationsGSS gss();
    
    public interface NotificationsGSS extends CssResource {
        public String notification();
        public String shown();
        public String hidden();
    }
}
