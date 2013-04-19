package com.sap.sailing.domain.racelog;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.impl.RaceLogEventFactoryImpl;

public interface RaceLogEventFactory {
    RaceLogEventFactory INSTANCE = new RaceLogEventFactoryImpl();

    RaceLogFlagEvent createFlagEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);
    RaceLogFlagEvent createFlagEvent(TimePoint timePoint, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);

    RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, Serializable id, List<Competitor> involvedBoats, int passId, TimePoint startTime);
    RaceLogStartTimeEvent createStartTimeEvent(TimePoint timePoint, int passId, TimePoint startTime);

    RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus);
    RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint timePoint, int passId, RaceLogRaceStatus nextStatus);

    RaceLogPassChangeEvent createPassChangeEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId);
    RaceLogPassChangeEvent createPassChangeEvent(TimePoint timePoint, int passId);

    RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId);
    RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint timePoint, int passId, Serializable courseAreaId);
    
    RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId, CourseBase courseData);
    RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint timePoint, int passId, CourseBase courseData);
    
    RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors);
    RaceLogFinishPositioningListChangedEvent createFinishPositioningListChangedEvent(TimePoint timePoint, int passId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors);
    
    RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint timePoint, Serializable id, List<Competitor> competitors, int passId);
    RaceLogFinishPositioningConfirmedEvent createFinishPositioningConfirmedEvent(TimePoint timePoint, int passId);
}
