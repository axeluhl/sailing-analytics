package com.sap.sse.security.ui.client.component.usergroup.roles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources used by the authentication framework.
 */
public interface UserGroupRoleResources extends ClientBundle {

    public static final UserGroupRoleResources INSTANCE = GWT.create(UserGroupRoleResources.class);

    @Source("UserGroupRoleResources.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String roleDefinitionSuggest();
    }
}
