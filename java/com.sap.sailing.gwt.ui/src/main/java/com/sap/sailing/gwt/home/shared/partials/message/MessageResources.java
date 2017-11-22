package com.sap.sailing.gwt.home.shared.partials.message;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface MessageResources extends ClientBundle {
    public static final MessageResources INSTANCE = GWT.create(MessageResources.class);

    @Source("Message.gss")
    LocalCss css();
    
    @Source("notification.png")
    ImageResource notification();

    public interface LocalCss extends CssResource {
        String message();
        String message_icon();
        String message_text();
        String message_close();
    }
}
