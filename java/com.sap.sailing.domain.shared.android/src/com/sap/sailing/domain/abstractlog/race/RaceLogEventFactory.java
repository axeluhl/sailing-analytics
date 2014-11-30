package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventFactoryImpl;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.tracking.Wind;
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

    DeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to);

    DeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from, TimePoint to);

    DenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId,
    		String raceName, BoatClass boatClass, Serializable raceId);

    StartTrackingEvent createStartTrackingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId);
    
    RaceLogRevokeEvent createRevokeEvent(AbstractLogEventAuthor author, int passId, RaceLogEvent toRevoke, String reason);
    
    RegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Competitor competitor);
    
    DefineMarkEvent createDefineMarkEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId, Mark mark);
    
    CloseOpenEndedDeviceMappingEvent createCloseOpenEndedDeviceMappingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId,
            Serializable deviceMappingEventId, TimePoint closingTimePoint);
    
    RaceLogEvent createFixedMarkPassingEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, Integer passId, TimePoint ofFixedPassing, Integer zeroBasedIndexOfWaypoint);

    AdditionalScoringInformationEvent createAdditionalScoringInformationEvent(TimePoint timePoint, Serializable id, AbstractLogEventAuthor author, int currentPassId,
            AdditionalScoringInformationType informationType);

    RaceLogEvent createSuppressedMarkPassingsEvent(TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id,
            List<Competitor> competitors, Integer passId, Integer zeroBasedIndexOfFirstSuppressedWaypoint);
}
