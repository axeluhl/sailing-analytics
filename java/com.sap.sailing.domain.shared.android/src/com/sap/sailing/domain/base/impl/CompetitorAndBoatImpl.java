package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoat;

/**
 * A utility class to hold a combination of a competitor and a boat
 * @author Frank Mittag
 *
 */
public class CompetitorAndBoatImpl implements CompetitorAndBoat {
    private static final long serialVersionUID = 6057339511208358777L;
    private final Competitor competitor;
    private final Boat boat;

    public CompetitorAndBoatImpl(Competitor competitor, Boat boat) {
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
