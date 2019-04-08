package com.sap.sailing.gwt.home.desktop.partials.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface MainMediaResources extends ClientBundle {
    public static final MainMediaResources INSTANCE = GWT.create(MainMediaResources.class);

    @Source("MainMedia.gss")
    LocalCss css();
    
    @Source("media-controls__arrows.png")
    ImageResource mediaControlsArrows();
    
    @Source("media-controls__fullsize.png")
    ImageResource mediaControlsFullsize();
    
    @Source("video__preview--play.png")
    ImageResource videoPreviewPlay();

    public interface LocalCss extends CssResource {
        String media();
        String media_swipecontainer();
        String media_swipewrapper();
        String media_swiperslide();
        String media_slideshow_controls();
        String media_slideshow_controlsnext();
        String media_slideshow_controlsprev();
        String media_slideshow_controlsfullsize();
        String mediavideos();
        String mainsection_header_title();
        String videopreview();
        String videopreview_videocontainer();
        String videopreview_videocontainer_video();
        String videopreview_title();
    }
}
