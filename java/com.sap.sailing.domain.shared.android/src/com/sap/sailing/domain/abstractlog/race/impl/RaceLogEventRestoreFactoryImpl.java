package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.RevokeEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.impl.AdditionalScoringInformationEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.events.CloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.DefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.DenoteForTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.DeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.DeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.RegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.RevokeEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.events.StartTrackingEventImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
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

    @Override
    public DeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to) {
        return new DeviceCompetitorMappingEventImpl(createdAt, author, logicalTimePoint, pId, passId, mappedTo, device, from, to);
    }

    @Override
    public DeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from,
            TimePoint to) {
        return new DeviceMarkMappingEventImpl(createdAt, author, logicalTimePoint, pId, passId, mappedTo, device, from, to);
    }

    @Override
    public DenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, int passId, String raceName, BoatClass boatClass, Serializable raceId) {
        return new DenoteForTrackingEventImpl(createdAt, author, logicalTimePoint, pId, passId, raceName, boatClass, raceId);
    }

    @Override
    public StartTrackingEvent createStartTrackingEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId) {
        return new StartTrackingEventImpl(createdAt, author, logicalTimePoint, pId, passId);
    }

    @Override
    public RevokeEvent createRevokeEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo, String reason) {
        return new RevokeEventImpl(createdAt, author, logicalTimePoint, pId, passId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }

    @Override
    public RegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Competitor competitor) {
        return new RegisterCompetitorEventImpl(createdAt, author, logicalTimePoint, pId, passId, competitor);
    }

    @Override
    public DefineMarkEvent createDefineMarkEvent(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Mark mark) {
        return new DefineMarkEventImpl(createdAt, author, logicalTimePoint, pId, passId, mark);
    }

    @Override
    public CloseOpenEndedDeviceMappingEvent createCloseOpenEndedDeviceMappingEvent(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint logicalTimePoint, Serializable pId, int passId,
            Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        return new CloseOpenEndedDeviceMappingEventImpl(createdAt, author, logicalTimePoint, pId, passId,
                deviceMappingEventId, closingTimePoint);
    }

    @Override
    public AdditionalScoringInformationEvent createAdditionalScoringInformationEvent(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint logicalTimePoint, Serializable pId, List<Competitor> competitors, int passId,
            AdditionalScoringInformationType informationType) {
        return new AdditionalScoringInformationEventImpl(createdAt, author, logicalTimePoint, pId, competitors, passId, informationType);
    }
}
