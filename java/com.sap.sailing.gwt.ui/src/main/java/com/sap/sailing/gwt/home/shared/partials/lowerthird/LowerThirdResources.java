package com.sap.sailing.gwt.home.shared.partials.lowerthird;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface LowerThirdResources extends ClientBundle {
    public static final LowerThirdResources INSTANCE = GWT.create(LowerThirdResources.class);

    @Source("LowerThird.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String info();
        String video_content_item_info();
        String video_content_item_info_text();
        String video_content_item_info_text_title();
        String video_content_item_info_text_titlesmall();
        String label();
        String video_content_item_info_button();
    }
}
