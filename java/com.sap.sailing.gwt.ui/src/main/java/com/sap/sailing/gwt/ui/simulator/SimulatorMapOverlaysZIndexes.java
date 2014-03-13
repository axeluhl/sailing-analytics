package com.sap.sailing.gwt.ui.simulator;


/**
 * Defines constants for the Z-Indexes for all race map canvas overlays to achieve the right z ordering of the layers.
 * @author Frank
 *
 */
public interface SimulatorMapOverlaysZIndexes {
    public static int BASE_CANVAS_OVERLAY_ZINDEX = 210;
    
    public static int REGATTA_AREA_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 1;
    public static int RACE_COURSE_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 2;
    public static int WINDGRID_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 3;
    public static int WINDFIELD_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 4;
    public static int WINDSTREAMLETS_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 5;
    public static int WINDLINE_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 6;
    public static int PATH_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 7;
    public static int WIND_ROSE_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 8;
    public static int PATHLEGEND_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 9;

    /** the z-index of the info window should always be higher than the rest */
    public static int INFO_WINDOW_ZINDEX = BASE_CANVAS_OVERLAY_ZINDEX + 50;
}
