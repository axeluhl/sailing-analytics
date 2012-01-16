package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class CompetitorImpl extends NamedImpl implements Competitor {
    private final Team team;
    private final Boat boat;
    private final Object id;
    
    public CompetitorImpl(Object id, String name, Team team, Boat boat) {
        super(name);
        this.id = id;
        this.team = team;
        this.boat = boat;
    }
    
    @Override
    public Object getId() {
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

}
