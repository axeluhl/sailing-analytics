package com.sap.sailing.gwt.home.desktop.partials.databylogo;

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
    DataResource tractrac();

    public interface LocalCss extends CssResource {
        String eventheader_dataBy_logo_container();
        String eventheader_dataBy_logo();
        String eventheader_dataBy_subtleLink();
        String eventheader_intro_details_item();
    }
}
