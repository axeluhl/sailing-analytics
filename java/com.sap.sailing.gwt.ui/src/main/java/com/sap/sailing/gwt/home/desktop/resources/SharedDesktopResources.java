package com.sap.sailing.gwt.home.desktop.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public interface SharedDesktopResources extends SharedHomeResources {
    
    public static final SharedDesktopResources INSTANCE = GWT.create(SharedDesktopResources.class);
    
    @Source("dropdown__check@2x.png")
    ImageResource dropdownCheck();
    
    @Source("liveraces.svg")
    @MimeType("image/svg+xml")
    DataResource liveraces();
    
    @Source("icon-audio.png")
    ImageResource audio();
    
    @Source("icon-video.png")
    ImageResource video();
    
    @Source("icon-wind.png")
    ImageResource wind();
    
    @Source("raw_gps_fixes.png")
    ImageResource gpsFixes();
    
    @Source("arrow-down-filled-black.png")
    ImageResource arrowDownFilledBlack();
}
