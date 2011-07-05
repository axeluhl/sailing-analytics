package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDAO implements IsSerializable {
    public String name;
    public List<RegattaDAO> regattas;
    public List<CompetitorDAO> competitors;
    
    public EventDAO() {}
    
    public EventDAO(String name, List<RegattaDAO> regattas, List<CompetitorDAO> competitors) {
        super();
        this.name = name;
        this.regattas = regattas;
    }
}
