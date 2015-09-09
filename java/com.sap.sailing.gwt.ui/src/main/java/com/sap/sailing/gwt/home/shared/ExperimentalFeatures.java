package com.sap.sailing.gwt.home.shared;

/**
 * This class contains convenience boolean flags to enable/ disable features in experimental phase.
 */
public class ExperimentalFeatures {

    /**
     * Show new races list
     */
    public static final boolean SHOW_NEW_RACES_LIST = true;
    /**
     * Show new regatta list
     */
    public static final boolean SHOW_NEW_REGATTA_LIST = true;
    /**
     * Show overview tab for single-regatta events and series events.
     */
    public static final boolean SHOW_SINGLE_REGATTA_OVERVIEW = true;
    /**
     * Provide option to show races in competition format
     */
    public static final boolean SHOW_RACES_COMPETITION_FORMAT = true;
    /**
     * Provide option to show the overview for regattas in multi regatta events and a navigation 
     * to the races view (competition format) for single regatta and series events on mobile devices 
     */
    public static final boolean SHOW_REGATTA_OVERVIEW_AND_RACES_ON_MOBILE = true;
    /**
     * Provide option to show the regatta progress for single regatta and series event on mobile devices 
     */
    public static final boolean SHOW_REGATTA_PROGRESS_ON_MOBILE = true;
    /**
     * Provide option to show the regattas live races for single regatta and series event on mobile devices 
     */
    public static final boolean SHOW_REGATTA_LIVE_RACES_ON_MOBILE = true;
    /**
     * Provide option to show the media page for mobile devices 
     */
    public static final boolean SHOW_MEDIA_PAGE_ON_MOBILE = true;
    /**
     * Provide button to show the competitor analytics in fullscreen view (work in progress)
     */
    public static final boolean SHOW_COMPETITOR_ANALYTICS_FULLSCREEN_VIEWER = false;
}
