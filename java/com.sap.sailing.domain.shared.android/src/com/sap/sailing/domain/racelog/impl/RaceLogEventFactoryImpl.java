package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
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
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.racelog.tracking.RevokeEvent;
import com.sap.sailing.domain.racelog.tracking.events.RegisterCompetitorEventImpl;
import com.sap.sailing.domain.racelog.tracking.events.StartTrackingEventImpl;
import com.sap.sailing.domain.racelog.tracking.events.DenoteForTrackingEventImpl;
import com.sap.sailing.domain.racelog.tracking.events.DeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.racelog.tracking.events.DeviceMarkMappingEventImpl;
import com.sap.sailing.domain.racelog.tracking.events.RevokeEventImpl;
import com.sap.sailing.domain.tracking.Wind;

public class RaceLogEventFactoryImpl implements RaceLogEventFactory {

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, Serializable id,
            List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        return new RaceLogFlagEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, involvedBoats, passId,
                upperFlag, lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogFlagEvent createFlagEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId, Flags upperFlag,
            Flags lowerFlag, boolean isDisplayed) {
        return createFlagEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, upperFlag,
                lowerFlag, isDisplayed);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime) {
        return new RaceLogStartTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, involvedBoats,
                passId, startTime);
    }

    @Override
    public RaceLogStartTimeEvent createStartTimeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId, TimePoint startTime) {
        return createStartTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, startTime);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus) {
        return new RaceLogRaceStatusEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, nextStatus);
    }

    @Override
    public RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId, RaceLogRaceStatus nextStatus) {
        return createRaceStatusEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, nextStatus);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId) {
        return new RaceLogPassChangeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors, passId);
    }

    @Override
    public RaceLogPassChangeEvent createPassChangeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId) {
        return createPassChangeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId) {
        return new RaceLogCourseAreaChangeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, courseAreaId);
    }

    @Override
    public RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            int passId, Serializable courseAreaId) {
        return createCourseAreaChangedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, courseAreaId);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, CourseBase courseData) {
        return new RaceLogCourseDesignChangedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, courseData);
    }

    @Override
    public RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            int passId, CourseBase courseData) {
        return createCourseDesignChangedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, courseData);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(TimePoint logicalTimePoint,
            RaceLogEventAuthor author, Serializable id, List<Competitor> competitors,
            int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint,
                id, competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningListChangedEventImpl createFinishPositioningListChangedEvent(TimePoint logicalTimePoint,
            RaceLogEventAuthor author, int passId, CompetitorResults positionedCompetitors) {
        return createFinishPositioningListChangedEvent(logicalTimePoint, author, UUID.randomUUID(),
                new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint logicalTimePoint,
            RaceLogEventAuthor author, Serializable id, List<Competitor> competitors, int passId, CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningConfirmedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id,
                competitors, passId, positionedCompetitors);
    }

    @Override
    public RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            int passId, CompetitorResults positionedCompetitors) {
        return createFinishPositioningConfirmedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, positionedCompetitors);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, String pathfinderId) {
        return new RaceLogPathfinderEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, pathfinderId);
    }

    @Override
    public RaceLogPathfinderEvent createPathfinderEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId, String pathfinderId) {
        return createPathfinderEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, pathfinderId);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, Long gateLaunchStopTime, Long golfDownTime) {
        return new RaceLogGateLineOpeningTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors,
                passId, gateLaunchStopTime, golfDownTime);
    }

    @Override
    public RaceLogGateLineOpeningTimeEvent createGateLineOpeningTimeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            int passId, Long gateLaunchStopTime, Long golfDownTime) {
        return createGateLineOpeningTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, gateLaunchStopTime, golfDownTime);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, RacingProcedureType type) {
        return new RaceLogStartProcedureChangedEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id,
                competitors, passId, type);
    }

    @Override
    public RaceLogStartProcedureChangedEvent createStartProcedureChangedEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            int passId, RacingProcedureType type) {
        return createStartProcedureChangedEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, type);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            Serializable id, List<Competitor> competitors, int passId, TimePoint protestStartTime) {
        return new RaceLogProtestStartTimeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id,
                competitors, passId, protestStartTime);
    }

    @Override
    public RaceLogProtestStartTimeEvent createProtestStartTimeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
            int passId, TimePoint protestStartTime) {
        return createProtestStartTimeEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(),
                passId, protestStartTime);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId, Wind wind) {
        return createWindFixEvent(logicalTimePoint, author, UUID.randomUUID(), new ArrayList<Competitor>(), passId, wind);
    }

    @Override
    public RaceLogWindFixEvent createWindFixEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, Serializable id,
            List<Competitor> competitors, int passId, Wind wind) {
        return new RaceLogWindFixEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, id, competitors, passId, wind);
    }

	@Override
	public DeviceCompetitorMappingEvent createDeviceCompetitorMappingEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
			DeviceIdentifier device, Competitor mappedTo, int passId, TimePoint from, TimePoint to) {
		return new DeviceCompetitorMappingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(),
				passId, mappedTo, device, from, to);
	}

	@Override
	public DeviceMarkMappingEvent createDeviceMarkMappingEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
			DeviceIdentifier device, Mark mappedTo, int passId, TimePoint from, TimePoint to) {
		return new DeviceMarkMappingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(),
				passId, mappedTo, device, from, to);
	}
	
	@Override
	public DenoteForTrackingEvent createDenoteForTrackingEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId,
			String raceName, BoatClass boatClass) {
		return new DenoteForTrackingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId, raceName, boatClass);
	}
	
	@Override
	public StartTrackingEvent createStartTrackingEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId) {
		return new StartTrackingEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId);
	}
	
	@Override
	public RevokeEvent createRevokeEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author, int passId, Serializable revokedEventId) {
		return new RevokeEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId, revokedEventId);
	}
    
    @Override
    public RegisterCompetitorEvent createRegisterCompetitorEvent(TimePoint logicalTimePoint, RaceLogEventAuthor author,
    		int passId, Competitor competitor) {
    	return new RegisterCompetitorEventImpl(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), passId, competitor);
    }

}
