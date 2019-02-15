package com.sap.sse.security.ui.client.usermanagement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources used by the authentication framework.
 */
public interface RoleAndPermissionDetailsResources extends ClientBundle {

    public static final RoleAndPermissionDetailsResources INSTANCE = GWT.create(RoleAndPermissionDetailsResources.class);

    @Source("RoleAndPermissionDetailsResources.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String enterRoleNameSuggest();

        String enterPermissionSuggest();
    }
}
