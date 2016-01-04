package com.sap.sailing.gwt.home.shared.partials.countdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface CountdownResources extends ClientBundle {
    public static final CountdownResources INSTANCE = GWT.create(CountdownResources.class);

    @Source("Countdown.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String countdown();
        String countdown_image();
        String countdown_content();
        String countdown_content_title();
        String countdown_info();
        String countdown_info_text();
        String countdown_info_text_title();
        String countdown_info_lable();
        String countdown_info_button();
        String button();
    }
}
