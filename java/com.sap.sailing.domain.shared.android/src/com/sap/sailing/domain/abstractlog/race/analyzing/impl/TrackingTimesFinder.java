package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLogImpl;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class TrackingTimesFinder extends RaceLogAnalyzer<Util.Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> {

    public TrackingTimesFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Util.Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> performAnalysis() {
        boolean startOfTrackingFound = false;
        boolean endOfTrackingFound = false;
        TimePointSpecificationFoundInLog start = null;
        TimePointSpecificationFoundInLog end = null;
        final RaceLog raceLog = getLog();
        for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
            if (!startOfTrackingFound && event instanceof RaceLogStartOfTrackingEvent) {
                start = new TimePointSpecificationFoundInLogImpl(event.getLogicalTimePoint());
                startOfTrackingFound = true;
            }
            if (!endOfTrackingFound && event instanceof RaceLogEndOfTrackingEvent) {
                end = new TimePointSpecificationFoundInLogImpl(event.getLogicalTimePoint());
                endOfTrackingFound = true;
            }
            if (startOfTrackingFound && endOfTrackingFound) {
                break;
            }
        }
        final Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> result;
        if (startOfTrackingFound || endOfTrackingFound) {
            result = new Util.Pair<>(start, end);
        } else {
            result = null;
        }
        return result;
    }

}
