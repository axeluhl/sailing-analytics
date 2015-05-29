package com.sap.sse.gwt.theme.client.component.sapheader2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface SAPHeaderResources extends ClientBundle {
    public static final SAPHeaderResources INSTANCE = GWT.create(SAPHeaderResources.class);

    @Source("SAPHeader2.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String siteheader();
        String siteheaderLeft();
        String siteheaderCentered();
        String siteheaderRight();
        String logo();
        String logotitle();
        String pagetitle();
    }
    
    @Source("logo-small@2x.png")
    ImageResource logo();
}
