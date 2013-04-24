package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogEventFactoryImpl implements RaceLogEventFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId,
            Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(MillisecondsTimePoint.now(), timePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint timePoint,
            int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return createFlagEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, 
             TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(MillisecondsTimePoint.now(), timePoint, id, involvedBoats, passId, startTime);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, int passId, TimePoint startTime) {
        return createStartTimeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, startTime);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint,Serializable id, List<Competitor> competitors, int passId,
            RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, nextStatus);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint,
            int passId, RaceLogRaceStatus nextStatus) {
        return createRaceStatusEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors,
            int passId) {
        return new RaceLogPassChangeEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint timePoint, int passId) {
        return createPassChangeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors,
            int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, courseAreaId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(
            TimePoint timePoint, int passId, Serializable courseAreaId) {
        return createCourseAreaChangedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint timePoint, Serializable id,
            List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, courseData);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint timePoint, int passId, CourseBase courseData) {
        return createCourseDesignChangedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(
            TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(
            TimePoint timePoint, int passId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        return createFinishPositioningListChangedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint timePoint,
            Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogFinishPositioningConfirmedEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint timePoint, int passId) {
        return createFinishPositioningConfirmedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint timePoint, Serializable id,
            List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, pathfinderId);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint timePoint, int passId, String pathfinderId) {
        return createPathfinderEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint timePoint, Serializable id,
            List<Competitor> competitors, int passId, Long gateLineOpeningTimeInMillis) {
        return new RaceLogGateLineOpeningTimeEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, gateLineOpeningTimeInMillis);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint timePoint, int passId,
            Long gateLineOpeningTimeInMillis) {
        return createGateLineOpeningTimeEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, gateLineOpeningTimeInMillis);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint timePoint, Serializable id,
            List<Competitor> competitors, int passId, StartProcedureType type) {
        return new RaceLogStartProcedureChangedEventImpl(MillisecondsTimePoint.now(), timePoint, id, competitors, passId, type);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint timePoint, int passId,
            StartProcedureType type) {
        return createStartProcedureChangedEvent(timePoint, UUID.randomUUID(), new ArrayList<Competitor>(), passId, type);
    }

}
