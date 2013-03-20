package com.sap.sailing.domain.racelog;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.impl.RaceLogEventRestoreFactoryImpl;

public interface RaceLogEventRestoreFactory extends RaceLogEventFactory {
    RaceLogEventRestoreFactory INSTANCE = new RaceLogEventRestoreFactoryImpl();

    RaceLogFlagEvent createFlagEvent(TimePoint createdAt, TimePoint logicalTimePoint, Serializable id,
            List<Competitor> involvedBoats, int passId, Flags upperFlag, Flags lowerFlag, boolean isDisplayed);

    RaceLogStartTimeEvent createStartTimeEvent(TimePoint createdAt, TimePoint logicalTimePoint, Serializable id,
            List<Competitor> involvedBoats, int passId, TimePoint startTime);

    RaceLogRaceStatusEvent createRaceStatusEvent(TimePoint createdAt, TimePoint logicalTimePoint, Serializable id,
            List<Competitor> competitors, int passId, RaceLogRaceStatus nextStatus);

    RaceLogPassChangeEvent createPassChangeEvent(TimePoint createdAt, TimePoint logicalTimePoint, Serializable id,
            List<Competitor> competitors, int passId);

    RaceLogCourseAreaChangedEvent createCourseAreaChangedEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, Serializable courseAreaId);

    RaceLogCourseDesignChangedEvent createCourseDesignChangedEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            Serializable id, List<Competitor> competitors, int passId, CourseData courseData);
}
