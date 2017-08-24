package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorAndBoatEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterCompetitorAndBoatEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterCompetitorAndBoatEventImpl extends BaseRegisterCompetitorAndBoatEventImpl<RaceLogEventVisitor> implements
        RaceLogRegisterCompetitorAndBoatEvent {
    private static final long serialVersionUID = 1395401990335109923L;
    private final RaceLogEventData raceLogEventData;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RaceLogRegisterCompetitorAndBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, int passId, Competitor competitor, Boat boat)
            throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, competitor, boat);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RaceLogRegisterCompetitorAndBoatEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Competitor competitor, Boat boat)
            throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), passId, competitor, boat);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public List<Competitor> getInvolvedCompetitors() {
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
