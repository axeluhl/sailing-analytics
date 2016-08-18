package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;

public class CompetitorWithBoatImpl implements CompetitorWithBoat {
    private static final long serialVersionUID = 6057339511208358777L;
    private final Competitor competitor;
    private final Boat boat;

    public CompetitorWithBoatImpl(Competitor competitor, Boat boat) {
        this.competitor = competitor;
        this.boat = boat;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    @Override
    public Boat getBoat() {
        return boat;
    }

}
