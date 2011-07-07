package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.RaceTracker;

public class RaceDAO implements IsSerializable {
    public String name;
    public Iterable<CompetitorDAO> competitors;
    
    /**
     * Tells if this race is currently being tracked live, meaning that a {@link RaceTracker} is
     * listening for incoming GPS fixes, mark passings etc., to update a {@link TrackedRace} object
     * accordingly.
     */
    public boolean currentlyTracked;
    
    public RaceDAO() {}

    public RaceDAO(String name, Iterable<CompetitorDAO> competitors, boolean currentlyTracked) {
        super();
        this.name = name;
        this.competitors = competitors;
        this.currentlyTracked = currentlyTracked;
    }
    
    
}
