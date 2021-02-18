package com.sap.sailing.gwt.home.shared.places;

public interface ShareablePlaceContext {
    /**
     * 
     * @return Returns the path parameters to reach the Place this context belongs to, conforming to the shared URL
     *         pattern for Places. Will return @null, if there are no path parameters to be found.
     */
    public String getContextAsPathParameters();
}
