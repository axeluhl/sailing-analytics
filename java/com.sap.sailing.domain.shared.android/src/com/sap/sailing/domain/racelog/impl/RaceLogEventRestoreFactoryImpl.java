package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogEventRestoreFactoryImpl extends RaceLogEventFactoryImpl implements RaceLogEventRestoreFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint createdAt, TimePoint logicalTimePoint, Serializable id,
            List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(createdAt, logicalTimePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint createdAt, TimePoint logicalTimePoint, Serializable id,
            List<Competitor> involvedBoats, int passId, TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(createdAt, logicalTimePoint, id, involvedBoats, passId, startTime);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(createdAt, logicalTimePoint, id, competitors, passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogPassChangeEventImpl(createdAt, logicalTimePoint, id, competitors, passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(createdAt, logicalTimePoint, id, competitors, passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint createdAt,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(createdAt, logicalTimePoint, id, competitors, passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint createdAt,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(createdAt, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint createdAt,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogFinishPositioningConfirmedEventImpl(createdAt, logicalTimePoint, id, competitors, passId);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(createdAt, logicalTimePoint, id, competitors, passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, Long gateLineOpeningTime) {
        return new RaceLogGateLineOpeningTimeEventImpl(createdAt, logicalTimePoint, id, competitors, passId, gateLineOpeningTime);
    }


}
