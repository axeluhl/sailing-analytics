package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterCompetitorEventImpl extends BaseRegisterCompetitorEventImpl<RaceLogEventVisitor> implements
        RaceLogRegisterCompetitorEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    private final RaceLogEventData raceLogEventData;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RaceLogRegisterCompetitorEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, int passId, Competitor competitor)
            throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, competitor);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RaceLogRegisterCompetitorEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Competitor competitor)
            throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), passId, competitor);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public List<Competitor> getInvolvedBoats() {
        return Collections.singletonList(getCompetitor());
    }

    @Override
    public String toString() {
        return raceLogEventData.toString();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
