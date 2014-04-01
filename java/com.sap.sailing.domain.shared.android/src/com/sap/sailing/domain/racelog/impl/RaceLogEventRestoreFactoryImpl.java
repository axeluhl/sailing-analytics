package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.tracking.Wind;

public class RaceLogEventRestoreFactoryImpl extends RaceLogEventFactoryImpl implements RaceLogEventRestoreFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(createdAt, author, logicalTimePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(createdAt, author, logicalTimePoint, id, involvedBoats, passId, startTime);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogPassChangeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningConfirmedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, Long gateLaunchStopTime,
            Long golfDownTime) {
        return new RaceLogGateLineOpeningTimeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, gateLaunchStopTime,
                golfDownTime);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type) {
        return new RaceLogStartProcedureChangedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, type);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime) {
        return new RaceLogProtestStartTimeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, protestStartTime);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, Integer passId, Wind wind) {
        return new RaceLogWindFixEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, wind);
    }


}
