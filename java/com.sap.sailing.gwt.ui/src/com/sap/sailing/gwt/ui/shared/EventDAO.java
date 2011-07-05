package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDAO implements IsSerializable {
    public String name;
    public Iterable<RegattaDAO> regattas;
    public Iterable<CompetitorDAO> competitors;
    
    public EventDAO() {}
    
    public EventDAO(String name, Iterable<RegattaDAO> regattas, Iterable<CompetitorDAO> competitors) {
        super();
        this.name = name;
        this.regattas = regattas;
    }
}
