package com.sap.sse.security.ui.client.component.usergroup.users;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources used by the authentication framework.
 */
public interface UserGroupUserResources extends ClientBundle {

    public static final UserGroupUserResources INSTANCE = GWT.create(UserGroupUserResources.class);

    @Source("UserGroupUserResources.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String userDefinitionSuggest();

        String filterUsers();
    }
}
