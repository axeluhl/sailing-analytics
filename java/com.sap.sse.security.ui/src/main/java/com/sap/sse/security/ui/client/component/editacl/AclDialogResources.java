package com.sap.sse.security.ui.client.component.editacl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources used by the authentication framework.
 */
public interface AclDialogResources extends ClientBundle {
    
    public static final AclDialogResources INSTANCE = GWT.create(AclDialogResources.class);

    @Source("AclDialogResources.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String allowedActionsTable();

        String deniedActionsTable();
    }
}
