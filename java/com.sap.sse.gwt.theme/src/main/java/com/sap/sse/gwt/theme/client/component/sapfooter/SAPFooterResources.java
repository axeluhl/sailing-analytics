package com.sap.sse.gwt.theme.client.component.sapfooter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SAPFooterResources extends ClientBundle {
    public static final SAPFooterResources INSTANCE = GWT.create(SAPFooterResources.class);

    @Source("com/sap/sse/gwt/theme/client/component/sapfooter/SAPFooter.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String sitefooter();
        String sitefooter_copyright();
        String sitefooter_links();
        String sitefooter_links_link();
        String sitefooter_language();
        String sitefooter_language_link();
    }
}
