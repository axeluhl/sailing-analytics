package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.common.TimePoint;

public class RaceLogDenoteForTrackingEventImpl extends RaceLogEventImpl implements RaceLogDenoteForTrackingEvent {
    private static final long serialVersionUID = 6937741283401976385L;

    private final String raceName;
    private final BoatClass boatClass;
    private final Serializable raceId;

    public RaceLogDenoteForTrackingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int pPassId, String raceName,
            BoatClass boatClass, Serializable raceId) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        this.raceName = raceName;
        this.boatClass = boatClass;
        this.raceId = raceId;
    }

    public RaceLogDenoteForTrackingEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            String raceName, BoatClass boatClass, Serializable raceId) {
        this(now(), logicalTimePoint, author, randId(), pPassId, raceName, boatClass, raceId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getRaceName() {
        return raceName;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public Serializable getRaceId() {
        return raceId;
    }

    @Override
    public String getShortInfo() {
        return getRaceName();
    }
}
