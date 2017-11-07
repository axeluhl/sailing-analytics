package com.sap.sailing.gwt.home.shared.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.client.ImageResource;

public interface SharedHomeResources extends ClientBundle {
    
    public static final SharedHomeResources INSTANCE = GWT.create(SharedHomeResources.class);
    
    public static final String GSS = "com/sap/sailing/gwt/home/shared/resources/SharedHomeRecources.gss";
    
    @Source("default_event_logo.jpg")
    ImageResource defaultEventLogoImage();
    
    @Source("default_event_photo.jpg")
    ImageResource defaultEventPhotoImage();
    
    @Source("default_stage_event_teaser.jpg")
    ImageResource defaultStageEventTeaserImage();
    
    @Source("default_video_preview.jpg")
    ImageResource defaultVideoPreviewImage();
    
    @Source("arrow-down-grey.png")
    ImageResource arrowDownGrey();
    
    @Source("arrow-grey-right.png")
    ImageResource arrowRightGrey();
    
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
    
    @Source("facebook-logo.svg")
    @MimeType("image/svg+xml")
    DataResource facebookLogo();
    
    @Source("twitter-logo.svg")
    @MimeType("image/svg+xml")
    DataResource twitterLogo();
    
    @Source("icon-green-check.svg")
    @MimeType("image/svg+xml")
    DataResource greenCheck();
    
    @Source("launch-loupe.svg")
    @MimeType("image/svg+xml")
    DataResource launchLoupe();
    
    @Source("launch-play.svg")
    @MimeType("image/svg+xml")
    DataResource launchPlay();
    
}
