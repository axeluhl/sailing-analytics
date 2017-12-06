package com.sap.sailing.gwt.home.shared.partials.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SolutionsSharedResources extends ClientBundle {
    public static final SolutionsSharedResources INSTANCE = GWT.create(SolutionsSharedResources.class);
    
    @Source("solutions-sap-in-sailing.jpg")
    ImageResource sapInSailing();
    
    @Source("solutions-sap-sailing-insight.png")
    ImageResource sapSailingInsight();
    
    @Source("solutions-sap-sailing-buoy-pinger.png")
    ImageResource sapSailingBuoyPinger();

}
