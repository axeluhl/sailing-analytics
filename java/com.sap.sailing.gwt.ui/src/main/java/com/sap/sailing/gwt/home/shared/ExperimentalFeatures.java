package com.sap.sailing.gwt.home.shared;

/**
 * This interface contains convenience boolean flags to enable/ disable features in experimental phase.
 */
public interface ExperimentalFeatures {

    /**
     * Provide buttons for user management login via social services like facebook or google.
     */
    public static final boolean SHOW_SOCIAL_LOGINS_FOR_USER_MANAGEMENT = false;
    /**
     * Provide a tab "subscription" in the user profile page.
     */
    public static final boolean SHOW_SUBSCRIPTION_IN_USER_PROFILE = true;
}
