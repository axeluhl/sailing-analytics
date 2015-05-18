package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
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

public class RaceLogEventRestoreFactoryImpl extends RaceLogEventFactoryImpl implements RaceLogEventRestoreFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(createdAt, author, logicalTimePoint, id, involvedBoats, passId, upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(createdAt, author, logicalTimePoint, id, involvedBoats, passId, startTime);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogPassChangeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningConfirmedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, Long gateLaunchStopTime,
            Long golfDownTime) {
        return new RaceLogGateLineOpeningTimeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, gateLaunchStopTime,
                golfDownTime);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type) {
        return new RaceLogStartProcedureChangedEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, type);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime) {
        return new RaceLogProtestStartTimeEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, protestStartTime);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, Integer passId, Wind wind) {
        return new RaceLogWindFixEventImpl(createdAt, author, logicalTimePoint, id, competitors, passId, wind);
    }

    @Override
    public RaceLogDeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to) {
        return new RaceLogDeviceCompetitorMappingEventImpl(createdAt, author, logicalTimePoint, pId, passId, mappedTo, device, from, to);
    }

    @Override
    public RaceLogDeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from,
            TimePoint to) {
        return new RaceLogDeviceMarkMappingEventImpl(createdAt, author, logicalTimePoint, pId, passId, mappedTo, device, from, to);
    }

    @Override
    public RaceLogDenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, int passId, String raceName, BoatClass boatClass, Serializable raceId) {
        return new RaceLogDenoteForTrackingEventImpl(createdAt, author, logicalTimePoint, pId, passId, raceName, boatClass, raceId);
    }

    @Override
    public RaceLogStartTrackingEvent createStartTrackingEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId) {
        return new RaceLogStartTrackingEventImpl(createdAt, author, logicalTimePoint, pId, passId);
    }

    @Override
    public RaceLogRevokeEvent createRevokeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo, String reason) {
        return new RaceLogRevokeEventImpl(createdAt, author, logicalTimePoint, pId, passId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }

    @Override
    public RaceLogRegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Competitor competitor) {
        return new RaceLogRegisterCompetitorEventImpl(createdAt, author, logicalTimePoint, pId, passId, competitor);
    }

    @Override
    public RaceLogDefineMarkEvent createDefineMarkEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Mark mark) {
        return new RaceLogDefineMarkEventImpl(createdAt, author, logicalTimePoint, pId, passId, mark);
    }

    @Override
    public RaceLogCloseOpenEndedDeviceMappingEvent createCloseOpenEndedDeviceMappingEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable pId, int passId,
            Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        return new RaceLogCloseOpenEndedDeviceMappingEventImpl(createdAt, author, logicalTimePoint, pId, passId,
                deviceMappingEventId, closingTimePoint);
    }

    @Override
    public RaceLogAdditionalScoringInformationEvent createAdditionalScoringInformationEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable pId, List<Competitor> competitors, int passId,
            AdditionalScoringInformationType informationType) {
        return new RaceLogAdditionalScoringInformationEventImpl(createdAt, author, logicalTimePoint, pId, competitors, passId, informationType);
    }

    @Override
    public RaceLogDependentStartTimeEvent createDependentStartTimeEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> involvedBoats,
            int passId, Fleet dependentOnFleet, Duration startTimeDifference) {
        return new RaceLogDependentStartTimeEventImpl(createdAt, author, logicalTimePoint, id, involvedBoats, passId,
                dependentOnFleet, startTimeDifference);
    }
}
