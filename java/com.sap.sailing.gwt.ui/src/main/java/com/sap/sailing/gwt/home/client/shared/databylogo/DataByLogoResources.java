package com.sap.sailing.gwt.home.client.shared.databylogo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public interface DataByLogoResources extends SharedHomeResources {
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
    }
}
