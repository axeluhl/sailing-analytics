package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class RaceDAO extends NamedDAO implements IsSerializable {
    public Iterable<CompetitorDAO> competitors;
    
    /**
     * Tells if this race is currently being tracked live, meaning that a {@link TracTracRaceTracker} is
     * listening for incoming GPS fixes, mark passings etc., to update a {@link TrackedRace} object
     * accordingly.
     */
    public boolean currentlyTracked;

    public Date startOfRace;
    public Date startOfTracking;
    public Date timePointOfLastEvent;
    public Date timePointOfNewestEvent;
    
    public RacePlaceOrder racePlaces = null;
    
    public RaceDAO() {}

    public RaceDAO(String name, Iterable<CompetitorDAO> competitors, boolean currentlyTracked) {
        super(name);
        this.competitors = competitors;
        this.currentlyTracked = currentlyTracked;
    }
    
    
}
