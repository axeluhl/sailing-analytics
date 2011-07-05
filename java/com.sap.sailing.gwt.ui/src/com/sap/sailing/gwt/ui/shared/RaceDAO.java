package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceDAO implements IsSerializable {
    public String name;
    public Iterable<CompetitorDAO> competitors;
    
    public RaceDAO() {}

    public RaceDAO(String name, Iterable<CompetitorDAO> competitors) {
        super();
        this.name = name;
        this.competitors = competitors;
    }
    
    
}
