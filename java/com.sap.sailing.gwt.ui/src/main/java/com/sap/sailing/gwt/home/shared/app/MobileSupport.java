package com.sap.sailing.gwt.home.shared.app;

/**
 * Marker for Places that have a mobile version depending on some place state. This is commonly uses in combination with
 * {@link com.sap.sailing.gwt.home.shared.ExperimentalFeatures} for new mobile places that should be able to be
 * deactivated during active development.
 */
public interface MobileSupport {
    
    boolean hasMobileVersion();

}
