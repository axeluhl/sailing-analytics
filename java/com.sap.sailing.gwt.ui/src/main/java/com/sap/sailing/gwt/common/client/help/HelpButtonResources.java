package com.sap.sailing.gwt.common.client.help;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface HelpButtonResources {

    ImageResource icon();

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
