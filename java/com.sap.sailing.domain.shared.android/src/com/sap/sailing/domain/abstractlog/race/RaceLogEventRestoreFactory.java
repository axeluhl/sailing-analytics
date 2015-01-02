package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventRestoreFactoryImpl;
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
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;

public interface RaceLogEventRestoreFactory extends RaceLogEventFactory {
    RaceLogEventRestoreFactory INSTANCE = new RaceLogEventRestoreFactoryImpl();

    RaceLogFlagEvent createFlagEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);

    RaceLogStartTimeEvent createStartTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime);

    RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus);

    RaceLogPassChangeEvent createPassChangeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId);

    RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId);

    RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, CourseBase courseData);

    RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors,
            int passId, CompetitorResults positionedCompetitors);

    RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, 
            int passId, CompetitorResults positionedCompetitors);

    RaceLogPathfinderEvent createPathfinderEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, String pathfinderId);

    RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, Long gateLaunchStopTime, 
            Long golfDownTime);

    RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type);
    
    RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime);

    RaceLogWindFixEvent createWindFixEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, Integer passId, Wind wind);

    RaceLogDeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to);

    RaceLogDeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from, TimePoint to);

    RaceLogDenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, int passId, String raceName, BoatClass boatClass, Serializable raceId);

    RaceLogStartTrackingEvent createStartTrackingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, int passId);
    
    RaceLogRevokeEvent createRevokeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, int passId, Serializable revokedEventId,
    		String revokedEventType, String revokedEventShortInfo, String reason);
    
    RaceLogRegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, int passId, Competitor competitor);
    
    RaceLogDefineMarkEvent createDefineMarkEvent(TimePoint createdAt, AbstractLogEventAuthor author,
                TimePoint logicalTimePoint, Serializable pId, int passId, Mark mark);
    
    RaceLogCloseOpenEndedDeviceMappingEvent createCloseOpenEndedDeviceMappingEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, int passId, Serializable deviceMappingEventId, TimePoint closingTimePoint);

    RaceLogAdditionalScoringInformationEvent createAdditionalScoringInformationEvent(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, List<Competitor> competitors, int passId,
            AdditionalScoringInformationType informationType);
}
