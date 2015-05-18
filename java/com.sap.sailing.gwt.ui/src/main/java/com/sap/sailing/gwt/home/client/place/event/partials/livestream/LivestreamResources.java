package com.sap.sailing.gwt.home.client.place.event.partials.livestream;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface LivestreamResources extends ClientBundle {
    public static final LivestreamResources INSTANCE = GWT.create(LivestreamResources.class);

    @Source("Livestream.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String videoitem();
        String playbutton();
        String info();
        String playicon();
        String videoplaceholder();
        String videoplaceholder_image();
        String livestream();
        String livestream_play();
        String livestream_info();
        String livestream_info_text();
        String label();
    }
}
