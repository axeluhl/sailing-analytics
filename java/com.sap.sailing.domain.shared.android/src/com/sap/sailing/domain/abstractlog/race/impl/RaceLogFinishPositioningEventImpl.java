package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public abstract class RaceLogFinishPositioningEventImpl extends RaceLogEventImpl implements RaceLogFinishPositioningEvent {

    private static final long serialVersionUID = -8168584588697908309L;
    
    private final CompetitorResults positionedCompetitors;

    public RaceLogFinishPositioningEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, CompetitorResults positionedCompetitors) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.positionedCompetitors = positionedCompetitors;
    }
    
    @Override
    public CompetitorResults getPositionedCompetitorsIDsNamesMaxPointsReasons() {
        return positionedCompetitors;
    }

    @Override
    public String getShortInfo() {
        return "positionedCompetitors=" + positionedCompetitors;
    }
}
