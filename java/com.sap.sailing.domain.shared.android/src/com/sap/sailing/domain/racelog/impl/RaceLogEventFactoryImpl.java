package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
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

public class RaceLogEventFactoryImpl implements RaceLogEventFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint timePoint, RaceLogEventAuthor author, Serializable id,
            List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, involvedBoats, passId,
                upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint timePoint, RaceLogEventAuthor author, int passId, Flags upperFlag,
            Flags lowerFlag, boolean isDisplayed) {
        return createFlagEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag,
                lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, involvedBoats,
                passId, startTime);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, RaceLogEventAuthor author, int passId, TimePoint startTime) {
        return createStartTimeEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, startTime);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors,
                passId, nextStatus);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint, RaceLogEventAuthor author, int passId, RaceLogRaceStatus nextStatus) {
        return createRaceStatusEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogPassChangeEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors, passId);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint timePoint, RaceLogEventAuthor author, int passId) {
        return createPassChangeEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors,
                passId, courseAreaId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            int passId, Serializable courseAreaId) {
        return createCourseAreaChangedEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors,
                passId, courseData);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            int passId, CourseBase courseData) {
        return createCourseDesignChangedEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(TimePoint timePoint,
            RaceLogEventAuthor author, Serializable id, List<Competitor> competitors,
            int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(MillisecondsTimePoint.now(), author, timePoint,
                id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(TimePoint timePoint,
            RaceLogEventAuthor author, int passId, CompetitorResults positionedCompetitors) {
        return createFinishPositioningListChangedEvent(timePoint, author, UUID.randomUUID(),
                new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint timePoint,
            RaceLogEventAuthor author, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningConfirmedEventImpl(MillisecondsTimePoint.now(), author, timePoint, id,
                competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            int passId, CompetitorResults positionedCompetitors) {
        return createFinishPositioningConfirmedEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors,
                passId, pathfinderId);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint timePoint, RaceLogEventAuthor author, int passId, String pathfinderId) {
        return createPathfinderEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Long gateLineOpeningTimeInMillis) {
        return new RaceLogGateLineOpeningTimeEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors,
                passId, gateLineOpeningTimeInMillis);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint timePoint, RaceLogEventAuthor author,
            int passId, Long gateLineOpeningTimeInMillis) {
        return createGateLineOpeningTimeEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, gateLineOpeningTimeInMillis);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type) {
        return new RaceLogStartProcedureChangedEventImpl(MillisecondsTimePoint.now(), author, timePoint, id,
                competitors, passId, type);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint timePoint, RaceLogEventAuthor author,
            int passId, RacingProcedureType type) {
        return createStartProcedureChangedEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, type);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint timePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime) {
        return new RaceLogProtestStartTimeEventImpl(MillisecondsTimePoint.now(), author, timePoint, id,
                competitors, passId, protestStartTime);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint timePoint, RaceLogEventAuthor author,
            int passId, TimePoint protestStartTime) {
        return createProtestStartTimeEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, protestStartTime);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint timePoint, RaceLogEventAuthor author, int passId, Wind wind) {
        return createWindFixEvent(timePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, wind);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint timePoint, RaceLogEventAuthor author, Serializable id,
            List<Competitor> competitors, int passId, Wind wind) {
        return new RaceLogWindFixEventImpl(MillisecondsTimePoint.now(), author, timePoint, id, competitors, passId, wind);
    }

}
