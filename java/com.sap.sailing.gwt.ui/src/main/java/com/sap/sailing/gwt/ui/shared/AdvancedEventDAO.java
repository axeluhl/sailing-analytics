package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.base.RacePlaceOrder;

public class AdvancedEventDAO extends EventDAO {

    public List<RacePlaceOrder> racePlaces;
    public Date startTime;
    
    public AdvancedEventDAO(String name, List<RegattaDAO> regattas, List<CompetitorDAO> competitors,
            List<RacePlaceOrder> racePlaces, Date startTime) {
        super(name, regattas, competitors);
        this.racePlaces = racePlaces;
        this.startTime = startTime;
    }
    
}
