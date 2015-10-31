package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogUseCompetitorsFromRaceLogEventImpl extends RaceLogEventImpl implements
    RaceLogUseCompetitorsFromRaceLogEvent {
    private static final long serialVersionUID = -5114645637316367845L;

    public RaceLogUseCompetitorsFromRaceLogEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, int passId)
            throws IllegalArgumentException {
        super(createdAt, author, logicalTimePoint, id, null, passId);
    }

    @Override
    public String toString() {
        return "This RaceLog uses the competitors registered on the RaceLog instead of the competitors on the RegattaLog";
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
