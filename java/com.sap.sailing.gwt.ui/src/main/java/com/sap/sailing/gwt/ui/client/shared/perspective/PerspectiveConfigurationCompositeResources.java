package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface PerspectiveConfigurationCompositeResources extends ClientBundle {
    public static final PerspectiveConfigurationCompositeResources INSTANCE = GWT.create(PerspectiveConfigurationCompositeResources.class);

    @Source("com/sap/sailing/gwt/ui/client/shared/perspective/PerspectiveConfigurationComposite.gss")
    LocalCss css();

    @Shared
    public interface LocalCss extends CssResource {
        String someClass();
    }
}
