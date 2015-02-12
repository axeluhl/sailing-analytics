package com.sap.sailing.gwt.home.client.shared.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface FooterResources extends ClientBundle {
    public static final FooterResources INSTANCE = GWT.create(FooterResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/footer/Footer.gss")
    LocalCss css();

    @Shared
    public interface LocalCss extends CssResource {
        String sitefooter();
        String sitefooter_copyright();
        String sitefooter_links();
        String sitefooter_links_link();
        String sitefooter_language();
        String sitefooter_language_link();
    }
}
