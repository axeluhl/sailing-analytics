package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class CompetitorImpl extends NamedImpl implements Competitor {
    private static final long serialVersionUID = 294603681016643157L;
    private final Team team;
    private final Boat boat;
    private final Serializable id;
    
    public CompetitorImpl(Serializable id, String name, Team team, Boat boat) {
        super(name);
        this.id = id;
        this.team = team;
        this.boat = boat;
    }
    
    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public Team getTeam() {
        return team;
    }

    @Override
    public Boat getBoat() {
        return boat;
    }

    @Override
    public Competitor resolve(DomainFactory domainFactory) {
        Competitor result = domainFactory.getOrCreateCompetitor(getId(), getName(), getTeam(), getBoat());
        return result;
    }
}
