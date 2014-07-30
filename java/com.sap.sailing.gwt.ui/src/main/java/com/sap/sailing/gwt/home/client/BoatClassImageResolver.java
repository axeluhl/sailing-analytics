package com.sap.sailing.gwt.home.client;

import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.domain.base.impl.BoatClassMasterdata;

public class BoatClassImageResolver {
    private static Map<String, ImageResource> boatClassIconsMap;
    private static BoatClassImageResources imageResources = BoatClassImageResources.INSTANCE;
    
    static {
        boatClassIconsMap = new HashMap<String, ImageResource>();

        boatClassIconsMap.put(BoatClassMasterdata._49ER.getDisplayName(), imageResources._49erIcon());
        
    }

    public static ImageResource getBoatClassIconResource(String displayName) {
        return boatClassIconsMap.get(displayName);
    }
}
