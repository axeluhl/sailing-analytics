package com.sap.sailing.gwt.home.shared.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SharedHomeResources extends ClientBundle {
    
    public static final SharedHomeResources INSTANCE = GWT.create(SharedHomeResources.class);
    
    @Source("default_stage_event_teaser.jpg")
    ImageResource defaultStageEventTeaserImage();
    
    @Source("default_video_preview.jpg")
    ImageResource defaultVideoPreviewImage();
    
}
