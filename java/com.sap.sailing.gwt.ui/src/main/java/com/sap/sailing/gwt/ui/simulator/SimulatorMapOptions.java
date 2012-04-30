package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.maps.client.MapUIOptions;
import com.google.gwt.maps.client.geom.Size;

public class SimulatorMapOptions {
    private final static int width = 500;
    private final static int height = 300;
    private final static boolean doubleClick = false;
    private final static boolean hybridMapType = false;
    private final static boolean keyboard = false;
    private final static boolean largeMapControl3d = false;
    private final static boolean mapTypeControl = false;
    private final static boolean menuMapTypeControl = false;
    private final static boolean normalMapType = true;
    private final static boolean physicalMapType = false;
    private final static boolean satelliteMapType = false;
    private final static boolean scaleControl = true;
    private final static boolean scrollwheel = true;
    private final static boolean smallZoomControl3d = true;

    public static MapUIOptions newInstance() {
        MapUIOptions mapOptions = MapUIOptions.newInstance(Size.newInstance(width, height));
        mapOptions.setDoubleClick(doubleClick);
        mapOptions.setHybridMapType(hybridMapType);
        mapOptions.setKeyboard(keyboard);
        mapOptions.setLargeMapControl3d(largeMapControl3d);
        mapOptions.setMapTypeControl(mapTypeControl);
        mapOptions.setMenuMapTypeControl(menuMapTypeControl);
        mapOptions.setNormalMapType(normalMapType);
        mapOptions.setPhysicalMapType(physicalMapType);
        mapOptions.setSatelliteMapType(satelliteMapType);
        mapOptions.setScaleControl(scaleControl);
        mapOptions.setScrollwheel(scrollwheel);
        mapOptions.setSmallZoomControl3d(smallZoomControl3d);

        return mapOptions;
    }
}