package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class RaceLogDependentStartTimeEventImpl extends RaceLogRaceStatusEventImpl implements RaceLogDependentStartTimeEvent, Revokable {

    private static final long serialVersionUID = -2555082771473210123L;
    private final Duration startTimeDifference;
    private SimpleRaceLogIdentifier dependentOnRace;

    public RaceLogDependentStartTimeEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, SimpleRaceLogIdentifier dependentOnRace,
            Duration startTimeDifference) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId, RaceLogRaceStatus.SCHEDULED);
        this.dependentOnRace = dependentOnRace;
        this.startTimeDifference = startTimeDifference;
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
