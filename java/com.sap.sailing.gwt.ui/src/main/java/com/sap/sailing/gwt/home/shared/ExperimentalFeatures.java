package com.sap.sailing.gwt.home.shared;

/**
 * This class contains convenience boolean flags to enable/ disable features in experimental phase.
 */
public class ExperimentalFeatures {

    /**
     * Show overview tab for single-regatta events and series events.
     */
    public static final boolean SHOW_SINGLE_REGATTA_OVERVIEW = true;
    
    /**
     * Show overview tab for regattas in multiregatta events.
     */
    public static final boolean SHOW_MULTIREGATTAEVENT_REGATTA_OVERVIEW = true;
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
    /**
     * Provide textbox to filter races by competitor in desktop regatta races tab
     */
    public static final boolean SHOW_RACES_BY_COMPETITOR_FILTER = true;
    /**
     * Using {@link com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay} to generically render the breadcrumbs in desktop UI (currently causes styling problems).
     */
    public static final boolean USE_NAVIGATION_PATH_DISPLAY_ON_DESKTOP = true;
    /**
     * Using {@link com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay} to generically render navigation menu extensions.
     */
    public static final boolean USE_NAVIGATION_PATH_DISPLAY_ON_MOBILE = true;
    /**
     * Enables/disables the link to the race board on mobile races view (competition format), in case of a tracked race
     */
    public static final boolean ENABLE_RACE_VIEWER_LINK_ON_MOBILE = true;
    /**
     * Provide selection to filter regattas by boat category on mobile multiregatta event overview
     */
    public static final boolean SHOW_BOAT_CATEGORY_FILTER_ON_MOBILE = true;
    /**
     * Provide a button to toogle autoplay in desktop fullscreen image gallery viewer.
     */
    public static final boolean SHOW_AUTOPLAY_IMAGES_ON_DESKTOP = true;
    /**
     * Provide a button to toogle autoplay in mobile fullscreen image gallery viewer.
     */
    public static final boolean SHOW_AUTOPLAY_IMAGES_ON_MOBILE = true;
}
