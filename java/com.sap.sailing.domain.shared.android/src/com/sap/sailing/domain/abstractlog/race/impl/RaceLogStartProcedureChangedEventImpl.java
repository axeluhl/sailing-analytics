package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;

public class RaceLogStartProcedureChangedEventImpl extends RaceLogEventImpl implements
        RaceLogStartProcedureChangedEvent {

    private static final long serialVersionUID = -8102049547516570034L;

    private final RacingProcedureType startProcedureType;

    public RaceLogStartProcedureChangedEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, RacingProcedureType startProcedureType) {
        super(createdAt, pTimePoint, author, pId, pInvolvedBoats, pPassId);
        this.startProcedureType = startProcedureType;
    }

    public RaceLogStartProcedureChangedEventImpl(TimePoint pTimePoint,
            AbstractLogEventAuthor author, int pPassId, RacingProcedureType startProcedureType) {
        super(pTimePoint, author, pPassId);
        this.startProcedureType = startProcedureType;
    }

    @Override
    public RacingProcedureType getStartProcedureType() {
        return startProcedureType;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "startProcedureType=" + startProcedureType;
    }
}
