package com.sap.sailing.domain.polars;

public interface PolarsChangedListener {

    /**
     * Is called when the polars of the boat class you registered the listener for changed
     */
    void polarsChanged();
    
}
