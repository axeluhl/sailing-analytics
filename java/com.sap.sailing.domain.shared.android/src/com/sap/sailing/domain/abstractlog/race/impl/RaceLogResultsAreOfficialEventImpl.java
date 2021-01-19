package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogResultsAreOfficialEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogResultsAreOfficialEventImpl extends RaceLogEventImpl implements RaceLogResultsAreOfficialEvent {
    private static final long serialVersionUID = -1796278009919318553L;

    public RaceLogResultsAreOfficialEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int pPassId) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
    }

    public RaceLogResultsAreOfficialEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId) {
        this(now(), logicalTimePoint, author, randId(), pPassId);
    }


    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "Indicates that the results of this race are now official";
    }
}
