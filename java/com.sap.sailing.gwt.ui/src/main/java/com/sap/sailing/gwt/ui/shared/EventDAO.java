package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDAO extends NamedDAO implements IsSerializable {
    public List<RegattaDAO> regattas;
    public List<CompetitorDAO> competitors;
    
    public EventDAO() {}
    
    public EventDAO(String name, List<RegattaDAO> regattas, List<CompetitorDAO> competitors) {
        super(name);
        this.name = name;
        this.regattas = regattas;
        this.competitors = competitors;
    }
}
