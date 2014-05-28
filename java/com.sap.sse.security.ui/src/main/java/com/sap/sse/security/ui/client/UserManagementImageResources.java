package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface UserManagementImageResources extends ClientBundle {
    
    public static final UserManagementImageResources INSTANCE = GWT.create(UserManagementImageResources.class);
    
    @Source("com/sap/sse/security/ui/images/user.png")
    ImageResource user();
    
    @Source("com/sap/sse/security/ui/images/user_small.png")
    ImageResource userSmall();
    
    @Source("com/sap/sse/security/ui/images/user_icon.png")
    ImageResource userIcon();
}
