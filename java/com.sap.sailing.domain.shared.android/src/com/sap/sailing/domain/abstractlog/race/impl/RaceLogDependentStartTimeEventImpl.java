package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class RaceLogDependentStartTimeEventImpl extends RaceLogRaceStatusEventImpl implements
        RaceLogDependentStartTimeEvent, Revokable {

    private static final long serialVersionUID = -2555082771473210123L;
    private final Duration startTimeDifference;
    private SimpleRaceLogIdentifier dependentOnRace;

    public RaceLogDependentStartTimeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, SimpleRaceLogIdentifier dependentOnRace,
            Duration startTimeDifference, RaceLogRaceStatus nextStatus) {
        super(createdAt, pTimePoint, author, pId, pPassId, nextStatus);
        this.dependentOnRace = dependentOnRace;
        this.startTimeDifference = startTimeDifference;
    }

    public RaceLogDependentStartTimeEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            SimpleRaceLogIdentifier dependentOnRace, Duration startTimeDifference) {
        this(now(), logicalTimePoint, author, randId(), pPassId, dependentOnRace, startTimeDifference, RaceLogRaceStatus.SCHEDULED);
    }

    @Override
    public Duration getStartTimeDifference() {
        return startTimeDifference;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit((RaceLogDependentStartTimeEvent) this);
    }

    @Override
    public String getShortInfo() {
        return "dependentOnRace=" + dependentOnRace + "startTimeDifference=" + startTimeDifference;
    }

    @Override
    public SimpleRaceLogIdentifier getDependentOnRaceIdentifier() {
        return dependentOnRace;
    }

}
