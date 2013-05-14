package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;

public class RaceLogStartProcedureChangedEventImpl extends RaceLogEventImpl implements
        RaceLogStartProcedureChangedEvent {

    private static final long serialVersionUID = -8102049547516570034L;

    private StartProcedureType startProcedureType;

    public RaceLogStartProcedureChangedEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, StartProcedureType startProcedureType) {
        super(createdAt, pTimePoint, pId, pInvolvedBoats, pPassId);

        this.startProcedureType = startProcedureType;
    }

    @Override
    public StartProcedureType getStartProcedureType() {
        return startProcedureType;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
