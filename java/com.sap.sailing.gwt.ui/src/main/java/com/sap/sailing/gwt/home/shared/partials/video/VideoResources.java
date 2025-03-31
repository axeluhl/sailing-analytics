package com.sap.sailing.gwt.home.shared.partials.video;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface VideoResources extends ClientBundle {
    public static final VideoResources INSTANCE = GWT.create(VideoResources.class);

    @Source("Video.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String videoitem();
        String playbutton();
        String info();
        String videoplaceholder();
        String videoplaceholder_image();
        String video();
        String video_content();
        String video_content_item();
        String video_content_item_play();
        String video_content_item_video();
        String video_content_item_info();
        String video_content_item_info_text();
        String video_content_item_info_text_title();
        String video_content_item_info_text_titlesmall();
        String label();
        String video_content_item_info_button();
    }
}
