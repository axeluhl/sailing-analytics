package com.sap.sailing.gwt.ui.shared.databylogo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface DataByLogoResources extends ClientBundle {
    public static final DataByLogoResources INSTANCE = GWT.create(DataByLogoResources.class);

    @Source("DataByLogo.gss")
    LocalCss css();
    
    @Source("tractrac-color.svg")
    @MimeType("image/svg+xml")
    DataResource tractracColor();
    
    @Source("tractrac-white.svg")
    @MimeType("image/svg+xml")
    DataResource tractracWhite();

    public interface LocalCss extends CssResource {
        String databylogo_logo();
        String databylogo_subtlelink();
        String databylogo_container();
        String databylogo_white_text();
        String databylogo_black_text();
        String databylogo_text();
    }
}
