package com.sap.sailing.gwt.home.desktop.partials.error;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ErrorMessageResources extends ClientBundle {
    public static final ErrorMessageResources INSTANCE = GWT.create(ErrorMessageResources.class);

    @Source("ErrorMessage.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String error_content();
        String error_message();
        String error_message_detail();
        String reload_page_message();
    }
}
