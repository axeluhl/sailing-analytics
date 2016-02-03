package com.sap.sailing.gwt.home.shared;

/**
 * This class contains convenience boolean flags to enable/ disable features in experimental phase.
 */
public class ExperimentalFeatures {

    /**
     * Provide button to show the competitor analytics in fullscreen view (work in progress)
     */
    public static final boolean SHOW_COMPETITOR_ANALYTICS_FULLSCREEN_VIEWER = false;
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
    /**
     * Enables/disables user management on desktop by showing/hiding the respective menu in the upper right corner.
     */
    public static final boolean SHOW_USER_MANAGEMENT_ON_DESKTOP = true;
    /**
     * Enables/disables user management on mobile UI by showing/hiding the respective menu item(s) in the upper right menu.
     */
    public static final boolean SHOW_USER_MANAGEMENT_ON_MOBILE = true;
    /**
     * Provide buttons for user management login via social services like facebook or google.
     */
    public static final boolean SHOW_SOCIAL_LOGINS_FOR_USER_MANAGEMENT = false;
}
