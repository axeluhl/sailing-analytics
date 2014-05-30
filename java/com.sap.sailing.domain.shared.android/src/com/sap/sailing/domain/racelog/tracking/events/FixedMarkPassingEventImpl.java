package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.FixedMarkPassingEvent;

public class FixedMarkPassingEventImpl extends RaceLogEventImpl implements FixedMarkPassingEvent {

    private final Competitor competitor;
    private final Waypoint waypoint;
    private final TimePoint ofFixedPassing;

    public FixedMarkPassingEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Competitor competitor, Waypoint waypoint,
            TimePoint ofFixedPassing) {
        super(createdAt, author, logicalTimePoint, pId, pInvolvedBoats, pPassId);
        this.competitor = competitor;
        this.waypoint = waypoint;
        this.ofFixedPassing = ofFixedPassing;
    }

    private static final long serialVersionUID = -1796278009919318553L;

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public Waypoint getWaypoint() {
        return waypoint;
    }

    @Override
    public TimePoint getTimePointOfPassing() {
        return ofFixedPassing;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        // TODO
    }

}
