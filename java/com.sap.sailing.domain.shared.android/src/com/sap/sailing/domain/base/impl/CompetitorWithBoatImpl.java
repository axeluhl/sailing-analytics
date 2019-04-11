package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

import java.io.Serializable;
import java.net.URI;

public class CompetitorWithBoatImpl extends CompetitorImpl implements DynamicCompetitorWithBoat {
    private static final long serialVersionUID = 22679449208503264L;
    private DynamicBoat boat;

    public CompetitorWithBoatImpl(Serializable id, String name, String shortName, Color color, String email,
                                  URI flagImage, DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile,
                                  String searchTag, DynamicBoat boat) {
        super(id, name, shortName, color, email, flagImage, team, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile,
                searchTag);
        this.boat = boat;
    }

    public CompetitorWithBoatImpl(Competitor competitor, DynamicBoat boat) {
        this(competitor.getId(), competitor.getName(), competitor.getShortName(), competitor.getColor(), competitor.getEmail(),
                competitor.getFlagImage(), (DynamicTeam) competitor.getTeam(), competitor.getTimeOnTimeFactor(),
                competitor.getTimeOnDistanceAllowancePerNauticalMile(), competitor.getSearchTag(), boat);
    }

    public Competitor resolve(SharedDomainFactory domainFactory) {
        final Competitor result;
        if (!hasBoat()) {
            // bug2822: this is a migrated competitor that had its default boat removed because it occurs in a boats-can-change regatta.
            // De-serialize as a CompetitorImpl, not as a CompetitorWithBoatImpl; here we can, other than during the migration itself
            // because there references to the CompetitorWithBoatImpl object may already exist that cannot easily be found and replaced.
            // Here, we at least must make sure not to force a CompetitorWithBoatImpl and allow for a CompetitorImpl.
            result = super.resolve(domainFactory);
        } else {
            result = domainFactory.getOrCreateCompetitorWithBoat(getId(), getName(), getShortName(), getColor(),
                    getEmail(), getFlagImage(), getTeam(), getTimeOnTimeFactor(),
                    getTimeOnDistanceAllowancePerNauticalMile(), getSearchTag(), getBoat());
        }
        return result;
    }

    @Override
    public DynamicBoat getBoat() {
        return boat;
    }

    @Override
    public void clearBoat() {
        this.boat = null;
    }

    @Override
    public boolean hasBoat() {
        return boat != null;
    }

    private String getShortInfo() {
        String shortName = getShortName();
        if (!hasBoat()) {
            return shortName;
        }
        String boatInfo = getBoat().getSailID() != null ? getBoat().getSailID() : getBoat().getName();
        if (shortName == null || shortName.length() == 0) {
            return boatInfo;
        }
        if (boatInfo == null || boatInfo.length() == 0) {
            return shortName;
        }
        return shortName + Competitor.DELIMITER_SHORT_NAME + boatInfo;
    }

    @Override
    public String getDisplayName() {
        String shortInfo = getShortInfo();
        if (shortInfo == null || shortInfo.length() == 0) {
            return getName();
        }
        return getShortInfo() + Competitor.DELIMITER_SAIL_ID + getName();
    }

    @Override
    public String toString() {
        return super.toString() + " with boat " + getBoat();
    }
}
