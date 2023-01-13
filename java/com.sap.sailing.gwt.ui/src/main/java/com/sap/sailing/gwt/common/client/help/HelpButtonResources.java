package com.sap.sailing.gwt.common.client.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface HelpButtonResources extends ClientBundle {
    static HelpButtonResources INSTANCE = GWT.create(HelpButtonResources.class);

    @Source("help.png")
    ImageResource icon();

    @Source("HelpButton.gss")
    Style style();

    interface Style extends CssResource {
        @ClassName("help-icon")
        String icon();

        @ClassName("help-popup")
        String popup();

        String content();

        String text();

        String link();
    }
}
