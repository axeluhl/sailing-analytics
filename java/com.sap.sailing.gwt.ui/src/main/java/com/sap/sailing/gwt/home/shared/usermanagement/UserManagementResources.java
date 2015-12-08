package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface UserManagementResources extends ClientBundle {
    
    public static final UserManagementResources INSTANCE = GWT.create(UserManagementResources.class);

    @Source("UserManagement.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String flyover();
        String flyover_content();
    }
    
}
