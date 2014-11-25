package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogStartProcedureChangedEventImpl extends RaceLogEventImpl implements
        RaceLogStartProcedureChangedEvent {

    private static final long serialVersionUID = -8102049547516570034L;

    private final RacingProcedureType startProcedureType;

    public RaceLogStartProcedureChangedEventImpl(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, RacingProcedureType startProcedureType) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);

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
