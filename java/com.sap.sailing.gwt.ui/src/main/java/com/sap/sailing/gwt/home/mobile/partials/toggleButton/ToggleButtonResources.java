package com.sap.sailing.gwt.home.mobile.partials.toggleButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ToggleButtonResources extends ClientBundle {
    public static final ToggleButtonResources INSTANCE = GWT.create(ToggleButtonResources.class);

    @Source("ToggleButton.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String togglebutton();
        String togglebutton_button();
    }
}
