package com.sap.sailing.gwt.ui.shared;

public interface SailingServiceConstants {
    /**
     * The maximum number of wind fixes that the sailing service will deliver for one call
     */
    int MAX_NUMBER_OF_WIND_FIXES_TO_DELIVER_IN_ONE_CALL = 10000;
    
    /**
     * The maximum number of chart samples that the sailing service will deliver per competitor for
     * one call
     */
    int MAX_NUMBER_OF_FIXES_TO_QUERY = 10000;
}
