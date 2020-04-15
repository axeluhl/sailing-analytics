package com.sap.sailing.gwt.home.shared.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.sap.sse.gwt.common.CommonIcons;

public interface SharedHomeResources extends CommonIcons {
    
    public static final SharedHomeResources INSTANCE = GWT.create(SharedHomeResources.class);
    
    @Source("default_event_logo.jpg")
    @ImageOptions(preventInlining = true)
    ImageResource defaultEventLogoImage();
    
    @Source("default_event_photo.jpg")
    @ImageOptions(preventInlining = true)
    ImageResource defaultEventPhotoImage();
    
    @Source("default_stage_event_teaser.jpg")
    @ImageOptions(preventInlining = true)
    ImageResource defaultStageEventTeaserImage();
    
    @Source("default_video_preview.jpg")
    @ImageOptions(preventInlining = true)
    ImageResource defaultVideoPreviewImage();

    @Source("arrow-down-grey.png")
    ImageResource arrowDownGrey();
    
    @Source("arrow-down-white.png")
    ImageResource arrowDownWhite();

    @Source("news-i.png")
    ImageResource news();
    
    @Source("close.svg")
    @MimeType("image/svg+xml")
    DataResource close();
    
    @Source("reload.svg")
    @MimeType("image/svg+xml")
    DataResource reload();
    
    @Source("settings.svg")
    @MimeType("image/svg+xml")
    DataResource settings();
    
    @Source("fullscreen.svg")
    @MimeType("image/svg+xml")
    DataResource fullscreen();
    
    @Source("icon-green-check.svg")
    @MimeType("image/svg+xml")
    DataResource greenCheck();
    
    @Source("launch-loupe.svg")
    @MimeType("image/svg+xml")
    DataResource launchLoupe();
    
    @Source("launch-play.svg")
    @MimeType("image/svg+xml")
    DataResource launchPlay();
    
    @Source("icon-audio.png")
    ImageResource audio();

    @Source("icon-video.png")
    ImageResource video();

    @Source("icon-wind.png")
    ImageResource wind();

    @Source("raw_gps_fixes.png")
    ImageResource gpsFixes();
}
