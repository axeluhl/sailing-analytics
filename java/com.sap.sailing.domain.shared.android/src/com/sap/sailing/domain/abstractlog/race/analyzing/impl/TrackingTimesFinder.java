package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLogImpl;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class TrackingTimesFinder extends RaceLogAnalyzer<Util.Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> {
    private final TrackingTimesEventFinder trackingTimesEventFinder;
    
    public TrackingTimesFinder(RaceLog raceLog) {
        super(raceLog);
        this.trackingTimesEventFinder = new TrackingTimesEventFinder(raceLog);
    }

    @Override
    protected Util.Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> performAnalysis() {
        Pair<RaceLogStartOfTrackingEvent, RaceLogEndOfTrackingEvent> preResult = trackingTimesEventFinder.performAnalysis();
        return preResult == null ? null :
            new Util.Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>(
                preResult.getA()==null?null:new TimePointSpecificationFoundInLogImpl(preResult.getA().getLogicalTimePoint()),
                preResult.getB()==null?null:new TimePointSpecificationFoundInLogImpl(preResult.getB().getLogicalTimePoint()));
    }
}
