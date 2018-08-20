package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface BigButtonResources extends ClientBundle {
    public static final BigButtonResources INSTANCE = GWT.create(BigButtonResources.class);

    @Source("BigButton.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String bigbutton();
        String bigbutton_button();
    }
}
