package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sse.common.TimePoint;

public abstract class RaceLogFinishPositioningEventImpl extends RaceLogEventImpl implements
        RaceLogFinishPositioningEvent {

    private static final long serialVersionUID = -8168584588697908309L;

    private final CompetitorResults positionedCompetitors;

    public RaceLogFinishPositioningEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, CompetitorResults positionedCompetitors) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.positionedCompetitors = positionedCompetitors;
    }

    public RaceLogFinishPositioningEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            CompetitorResults positionedCompetitors) {
        this(now(), logicalTimePoint, author, randId(), pPassId, positionedCompetitors);
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
