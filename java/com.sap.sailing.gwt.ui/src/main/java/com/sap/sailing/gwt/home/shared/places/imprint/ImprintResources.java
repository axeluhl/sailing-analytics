package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ImprintResources extends ClientBundle {
    public static final ImprintResources INSTANCE = GWT.create(ImprintResources.class);

    @Source("Imprintview.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {

        String outerPanel();

        String owner();

        String homepage();

        String listbox();

        String acknowledgement();

        String licenseText();

        String disclaimerItem();

        String disclaimerItemTitle();
        
        String majorHeader();
    }
}
