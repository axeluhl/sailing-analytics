package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;

public class CompetitorImpl implements DynamicCompetitor {
    private static final long serialVersionUID = 294603681016643157L;
    private final DynamicTeam team;
    private final DynamicBoat boat;
    private final Serializable id;
    private String name;
    
    public CompetitorImpl(Serializable id, String name, DynamicTeam team, DynamicBoat boat) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.boat = boat;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String newName) {
        this.name = newName;
    }
    
    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public DynamicTeam getTeam() {
        return team;
    }

    @Override
    public DynamicBoat getBoat() {
        return boat;
    }

    @Override
    public Competitor resolve(SharedDomainFactory domainFactory) {
        Competitor result = domainFactory.getCompetitorStore().getOrCreateCompetitor(getId(), getName(), getTeam(), getBoat());
        return result;
    }
}
