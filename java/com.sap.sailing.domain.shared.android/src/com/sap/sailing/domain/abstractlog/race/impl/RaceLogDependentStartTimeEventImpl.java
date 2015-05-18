package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class RaceLogDependentStartTimeEventImpl extends RaceLogRaceStatusEventImpl implements RaceLogDependentStartTimeEvent, Revokable {

    private static final long serialVersionUID = -2555082771473210123L;
    private final Duration startTimeDifference;
    private final Fleet dependentsOnFleet;

    public RaceLogDependentStartTimeEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Fleet dependentsOnFleet,
            Duration startTimeDifference) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId, RaceLogRaceStatus.SCHEDULED);
        this.dependentsOnFleet = dependentsOnFleet;
        this.startTimeDifference = startTimeDifference;
    }

    @Override
    public Duration getStartTimeDifference() {
        return startTimeDifference;
    }

    @Override
    public Fleet getDependentOnFleet() {
        return dependentsOnFleet;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit((RaceLogDependentStartTimeEvent) this);
    }

    @Override
    public String getShortInfo() {
        return "dependentOnFleet=" + dependentsOnFleet + "startTimeDifference=" + startTimeDifference;
    }

}
