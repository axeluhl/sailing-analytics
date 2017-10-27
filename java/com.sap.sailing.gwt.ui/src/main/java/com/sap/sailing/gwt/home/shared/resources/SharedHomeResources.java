package com.sap.sailing.gwt.home.shared.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
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
    
}
