package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;

public class CompetitorImpl extends NamedImpl implements Competitor {
    private final Team team;
    private final Boat boat;
    
    public CompetitorImpl(String name, Team team, Boat boat) {
        super(name);
        this.team = team;
        this.boat = boat;
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
