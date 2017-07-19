package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsAndBoatsFromRaceLogEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogUseCompetitorsAndBoatsFromRaceLogEventImpl extends RaceLogEventImpl implements
    RaceLogUseCompetitorsAndBoatsFromRaceLogEvent {
    private static final long serialVersionUID = -1859200551195623389L;

    public RaceLogUseCompetitorsAndBoatsFromRaceLogEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, int passId)
            throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, null, passId);
    }

    @Override
    public String toString() {
        return "This RaceLog uses the competitors and boats registered on the RaceLog instead of the boats on the RegattaLog";
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
