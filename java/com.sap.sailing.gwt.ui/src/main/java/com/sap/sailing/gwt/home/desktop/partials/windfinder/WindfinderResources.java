package com.sap.sailing.gwt.home.desktop.partials.windfinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface WindfinderResources extends ClientBundle {
    public static final WindfinderResources INSTANCE = GWT.create(WindfinderResources.class);

    @Source("Windfinder.gss")
    LocalCss css();

    @Source("windfinder-logo-white.png")
    ImageResource windfinderLogoWhite();

    @Source("windfinder-logo-red.png")
    ImageResource windfinderLogoRed();

    public interface LocalCss extends CssResource {

        String windfindercontrol();

        String windfindercontrol_icon();

        String windfindercontrol_text();

        String windfinderlaunchpad_content();

        String windfinderlaunchpad_content_item();

        String windfinderlaunchpad_content_item_icon();

        String windfinderlaunchpad_content_item_title();
    }
}
