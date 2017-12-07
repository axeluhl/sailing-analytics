package com.sap.sailing.gwt.home.mobile.partials.videogallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface VideoGalleryResources extends ClientBundle {
    public static final VideoGalleryResources INSTANCE = GWT.create(VideoGalleryResources.class);

    @Source("VideoGallery.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String videoitem();
        String playbutton();
        String info();
        String videoplaceholder();
        String videoplaceholder_image();
        String video();
        String videohideinfo();
        String video_info();
        String video_play();
        String video_info_text();
        String videogallery();
        String videogallery_video();
        String grid();
        String videogallery_video_info();
        String videogallery_video_info_title();
        String videogallery_video_info_subtitle();
    }
}
