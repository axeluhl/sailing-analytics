package com.sap.sailing.gwt.home.mobile.partials.section;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MobileSectionResources extends ClientBundle {
    public static final MobileSectionResources INSTANCE = GWT.create(MobileSectionResources.class);

    @Source("MobileSection.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String section();

        String sectionHeader();

        String sectionContent();
    }
}
