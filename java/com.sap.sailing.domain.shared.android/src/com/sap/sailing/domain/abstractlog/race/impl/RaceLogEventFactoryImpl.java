package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.impl.RaceLogAdditionalScoringInformationEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDenoteForTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogStartTrackingEventImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceLogEventFactoryImpl implements RaceLogEventFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, involvedBoats, passId,
                upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Flags upperFlag,
            Flags lowerFlag, boolean isDisplayed) {
        return createFlagEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag,
                lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, involvedBoats,
                passId, startTime);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, TimePoint startTime) {
        return createStartTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, startTime);
    }
    
    @Override
    public RaceLogDependentStartTimeEvent createDependentStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> involvedBoats, int passId, Fleet dependentsOnFleet, Duration startTimeDifference) {
        return new RaceLogDependentStartTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, involvedBoats,
                passId, dependentsOnFleet, startTimeDifference);
    }

    @Override
    public RaceLogDependentStartTimeEvent createDependentStartTimeEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Fleet dependentsOnFleet, Duration startTimeDifference) {
        return createDependentStartTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId,
                dependentsOnFleet, startTimeDifference);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, nextStatus);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, RaceLogRaceStatus nextStatus) {
        return createRaceStatusEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogPassChangeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors, passId);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId) {
        return createPassChangeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, courseAreaId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, Serializable courseAreaId) {
        return createCourseAreaChangedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, courseData);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, CourseBase courseData) {
        return createCourseDesignChangedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, List<Competitor> competitors,
            int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint,
                id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, CompetitorResults positionedCompetitors) {
        return createFinishPositioningListChangedEvent(logicalTimePoint, author, UUID.randomUUID(),
                new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningConfirmedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id,
                competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, CompetitorResults positionedCompetitors) {
        return createFinishPositioningConfirmedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, pathfinderId);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, String pathfinderId) {
        return createPathfinderEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Long gateLaunchStopTime, Long golfDownTime) {
        return new RaceLogGateLineOpeningTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, gateLaunchStopTime, golfDownTime);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, Long gateLaunchStopTime, Long golfDownTime) {
        return createGateLineOpeningTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, gateLaunchStopTime, golfDownTime);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type) {
        return new RaceLogStartProcedureChangedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id,
                competitors, passId, type);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, RacingProcedureType type) {
        return createStartProcedureChangedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, type);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime) {
        return new RaceLogProtestStartTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id,
                competitors, passId, protestStartTime);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, TimePoint protestStartTime) {
        return createProtestStartTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, protestStartTime);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Wind wind) {
        return createWindFixEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, wind);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, int passId, Wind wind) {
        return new RaceLogWindFixEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors, passId, wind);
    }

    @Override
    public RaceLogDeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to) {
        return new RaceLogDeviceCompetitorMappingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(),
                passId, mappedTo, device, from, to);
    }

    @Override
    public RaceLogDeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from, TimePoint to) {
        return new RaceLogDeviceMarkMappingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(),
                passId, mappedTo, device, from, to);
    }

    @Override
    public RaceLogDenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId,
            String raceName, BoatClass boatClass, Serializable raceId) {
        return new RaceLogDenoteForTrackingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId, raceName,
                boatClass, raceId);
    }

    @Override
    public RaceLogStartTrackingEvent createStartTrackingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId) {
        return new RaceLogStartTrackingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId);
    }

    @Override
    public RaceLogRevokeEvent createRevokeEvent(AbstractLogEventAuthor author, int passId, RaceLogEvent toRevoke, String reason) {
        return new RaceLogRevokeEventImpl(author, passId, toRevoke, reason);
    }

    @Override
    public RaceLogRegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, Competitor competitor) {
        return new RaceLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId, competitor);
    }

    @Override
    public RaceLogDefineMarkEvent createDefineMarkEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, Mark mark) {
        return new RaceLogDefineMarkEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId, mark);
    }

    @Override
    public RaceLogCloseOpenEndedDeviceMappingEvent createCloseOpenEndedDeviceMappingEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        return new RaceLogCloseOpenEndedDeviceMappingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint,
                UUID.randomUUID(), passId, deviceMappingEventId, closingTimePoint);
    }

    @Override
    public RaceLogAdditionalScoringInformationEvent createAdditionalScoringInformationEvent(TimePoint timePoint, Serializable id,
            AbstractLogEventAuthor author, int currentPassId, AdditionalScoringInformationType informationType) {
        return new RaceLogAdditionalScoringInformationEventImpl(MillisecondsTimePoint.now(), author, timePoint, id,
                Collections.<Competitor> emptyList(), currentPassId, informationType);
    }

    @Override
    public RaceLogEvent createFixedMarkPassingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, Integer passId, TimePoint ofFixedPassing, Integer zeroBasedIndexOfWaypoint) {
        return new FixedMarkPassingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors, passId, ofFixedPassing,
                zeroBasedIndexOfWaypoint);
    }

    @Override
    public RaceLogEvent createSuppressedMarkPassingsEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, Integer passId, Integer zeroBasedIndexOfFirstSuppressedWaypoint) {
        return new SuppressedMarkPassingsEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors, passId,
                zeroBasedIndexOfFirstSuppressedWaypoint);
    }
}
