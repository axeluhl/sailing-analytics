package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventFactoryImpl;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
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

public interface RaceLogEventFactory {
    RaceLogEventFactory INSTANCE = new RaceLogEventFactoryImpl();

    RaceLogFlagEvent createFlagEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id, List<Competitor> involvedBoats,
            int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);

    RaceLogFlagEvent createFlagEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Flags upperFlag,
            Flags lowerFlag, boolean isDisplayed);

    RaceLogStartTimeEvent createStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> involvedBoats, int passId, TimePoint startTime);

    RaceLogStartTimeEvent createStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, TimePoint startTime);

    RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus);

    RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, RaceLogRaceStatus nextStatus);

    RaceLogPassChangeEvent createPassChangeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, int passId);

    RaceLogPassChangeEvent createPassChangeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId);

    RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId);

    RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, Serializable courseAreaId);

    RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, CourseBase courseData);

    RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, CourseBase courseData);

    RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, List<Competitor> competitors,
            int passId, CompetitorResults positionedCompetitors);

    RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, CompetitorResults positionedCompetitors);

    RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors);

    RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, 
            int passId, CompetitorResults positionedCompetitors);

    RaceLogPathfinderEvent createPathfinderEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, int passId, String pathfinderId);

    RaceLogPathfinderEvent createPathfinderEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, String pathfinderId);

    RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Long gateLaunchStopTime, Long golfDownTime);

    RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, Long gateLaunchStopTime, Long golfDownTime);

    RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type);

    RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int passId, RacingProcedureType type);

    RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime);

    RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, TimePoint protestStartTime);

    RaceLogWindFixEvent createWindFixEvent(TimePoint eventTime, AbstractLogEventAuthor author, int currentPassId, Wind wind);
    
    RaceLogWindFixEvent createWindFixEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Wind wind);

    RaceLogDeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to);

    RaceLogDeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from, TimePoint to);

    RaceLogDenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId,
    		String raceName, BoatClass boatClass, Serializable raceId);

    RaceLogStartTrackingEvent createStartTrackingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId);
    
    RaceLogRevokeEvent createRevokeEvent(AbstractLogEventAuthor author, int passId, RaceLogEvent toRevoke, String reason);
    
    RaceLogRegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Competitor competitor);
    
    RaceLogDefineMarkEvent createDefineMarkEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Mark mark);
    
    RaceLogCloseOpenEndedDeviceMappingEvent createCloseOpenEndedDeviceMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId,
            Serializable deviceMappingEventId, TimePoint closingTimePoint);
    
    RaceLogEvent createFixedMarkPassingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, Integer passId, TimePoint ofFixedPassing, Integer zeroBasedIndexOfWaypoint);

    RaceLogAdditionalScoringInformationEvent createAdditionalScoringInformationEvent(TimePoint timePoint, Serializable id, AbstractLogEventAuthor author, int currentPassId,
            AdditionalScoringInformationType informationType);

    RaceLogEvent createSuppressedMarkPassingsEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, Integer passId, Integer zeroBasedIndexOfFirstSuppressedWaypoint);

    RaceLogDependentStartTimeEvent createDependentStartTimeEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, List<Competitor> involvedBoats, int passId, Fleet dependentsOnFleet,
            Duration startTimeDifference);

    RaceLogDependentStartTimeEvent createDependentStartTimeEvent(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Fleet dependentsOnFleet, Duration startTimeDifference);
}
