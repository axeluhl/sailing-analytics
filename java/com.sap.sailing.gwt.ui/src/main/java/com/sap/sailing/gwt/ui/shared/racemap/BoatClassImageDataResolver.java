package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

public class BoatClassImageDataResolver {
	private static Map<String, BoatClassImageData> boatImagesMap;
	
	private static BoatClassImageData defaultBoatImages;
	
    private static BoatImageResources boatImageResources = GWT.create(BoatImageResources.class);

    static {
    	boatImagesMap = new HashMap<String, BoatClassImageData>();
    	BoatClassImageData _470er = new BoatClassImageData("470", 4.70, 44, 72, 42, "470er");
    	_470er.setDownWindPortIcons(boatImageResources.class470er_DownWindPortIcon(), boatImageResources.class470er_DownWindPortIconSelected());
    	_470er.setDownWindStarboardIcons(boatImageResources.class470er_DownWindStarboardIcon(), boatImageResources.class470er_DownWindStarboardIconSelected());
    	_470er.setUpWindPortIcons(boatImageResources.class470er_UpWindPortIcon(), boatImageResources.class470er_UpWindPortIconSelected());
    	_470er.setUpWindStarboardIcons(boatImageResources.class470er_UpWindStarboardIcon(), boatImageResources.class470er_UpWindStarboardIconSelected());
    	_470er.setReachingPortIcons(boatImageResources.class470er_ReachingPortIcon(), boatImageResources.class470er_ReachingPortIconSelected());
    	_470er.setReachingStarboardIcons(boatImageResources.class470er_ReachingStarboardIcon(), boatImageResources.class470er_ReachingStarboardIconSelected());

    	BoatClassImageData _49er = new BoatClassImageData("49", 4.90, 42, 67, 31, "49er");
    	_49er.setDownWindPortIcons(boatImageResources.class49er_DownWindPortIcon(), boatImageResources.class49er_DownWindPortIconSelected());
    	_49er.setDownWindStarboardIcons(boatImageResources.class49er_DownWindStarboardIcon(), boatImageResources.class49er_DownWindStarboardIconSelected());
    	_49er.setUpWindPortIcons(boatImageResources.class49er_UpWindPortIcon(), boatImageResources.class49er_UpWindPortIconSelected());
    	_49er.setUpWindStarboardIcons(boatImageResources.class49er_UpWindStarboardIcon(), boatImageResources.class49er_UpWindStarboardIconSelected());
    	_49er.setReachingPortIcons(boatImageResources.class49er_ReachingPortIcon(), boatImageResources.class49er_ReachingPortIconSelected());
    	_49er.setReachingStarboardIcons(boatImageResources.class49er_ReachingStarboardIcon(), boatImageResources.class49er_ReachingStarboardIconSelected());

    	BoatClassImageData extreme40 = new BoatClassImageData("Extreme40", 12.2, 120, 153, 97);
    	extreme40.setDownWindPortIcons(boatImageResources.Extreme40_DownWindPortIcon(), boatImageResources.Extreme40_DownWindPortIconSelected());
    	extreme40.setDownWindStarboardIcons(boatImageResources.Extreme40_DownWindStarboardIcon(), boatImageResources.Extreme40_DownWindStarboardIconSelected());
    	extreme40.setUpWindPortIcons(boatImageResources.Extreme40_UpWindPortIcon(), boatImageResources.Extreme40_UpWindPortIconSelected());
    	extreme40.setUpWindStarboardIcons(boatImageResources.Extreme40_UpWindStarboardIcon(), boatImageResources.Extreme40_UpWindStarboardIconSelected());
    	extreme40.setReachingPortIcons(boatImageResources.Extreme40_ReachingPortIcon(), boatImageResources.Extreme40_ReachingPortIconSelected());
    	extreme40.setReachingStarboardIcons(boatImageResources.Extreme40_ReachingStarboardIcon(), boatImageResources.Extreme40_ReachingStarboardIconSelected());
    	
    	defaultBoatImages = _470er;
    	boatImagesMap.put(_470er.getMainBoatClassName(), _470er);
    	boatImagesMap.put(_49er.getMainBoatClassName(), _49er);
    	boatImagesMap.put(extreme40.getMainBoatClassName(), extreme40);
	}
	
	public static BoatClassImageData getBoatClassImages(String boatClassName) {
		BoatClassImageData result = boatImagesMap.get(boatClassName);
		if( result == null) {
			result = defaultBoatImages;
		}
		return result;
	}
}
