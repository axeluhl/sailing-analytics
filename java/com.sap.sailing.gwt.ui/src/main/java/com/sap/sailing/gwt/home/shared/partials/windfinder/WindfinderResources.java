package com.sap.sailing.gwt.home.shared.partials.windfinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

public interface WindfinderResources extends ClientBundle {
    public static final WindfinderResources INSTANCE = GWT.create(WindfinderResources.class);

    @Source("Windfinder.gss")
    LocalCss css();

    @Source("windfinder-logo.svg")
    TextResource windfinderLogo();

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
