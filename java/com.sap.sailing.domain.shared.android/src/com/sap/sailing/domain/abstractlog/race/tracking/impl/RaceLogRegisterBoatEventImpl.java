package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterBoatEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterBoatEventImpl extends BaseRegisterBoatEventImpl<RaceLogEventVisitor> implements
        RaceLogRegisterBoatEvent {
    private static final long serialVersionUID = -856679388199355195L;
    private final RaceLogEventData raceLogEventData;

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public RaceLogRegisterBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, int passId, Boat boat)
            throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, boat);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public RaceLogRegisterBoatEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Boat boat)
            throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), passId, boat);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public String toString() {
        return raceLogEventData.toString();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<Competitor> getInvolvedBoats() {
        return Collections.emptyList();
    }
}
