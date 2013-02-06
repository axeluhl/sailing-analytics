package com.sap.sailing.gwt.ui.shared.racemap;

/**
 * Defines constants for the Z-Indexes for all race map canvas overlays to achieve the right z ordering of the layers.
 * Attention: We also need to incorporate about the z-Indexes of the map markers.
 * The default value of the map markers seems to be 1000.  
 * @author Frank
 *
 */
public interface RaceMapOverlaysZIndexes {
    public static int BOATS_ZINDEX = 1100;
    public static int COURSEMARK_ZINDEX = 1200;
    public static int COMPETITOR_INFO_ZINDEX = 1300;
    public static int WINDSENSOR_ZINDEX = 1400;
}
