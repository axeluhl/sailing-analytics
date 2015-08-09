package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class TrackingTimesFinder extends RaceLogAnalyzer<Util.Pair<TimePoint, TimePoint>> {

    public TrackingTimesFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Util.Pair<TimePoint, TimePoint> performAnalysis() {
        boolean startOfTrackingFound = false;
        boolean endOfTrackingFound = false;
        TimePoint start = null;
        TimePoint end = null;
        final RaceLog raceLog = getLog();
        for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
            if (!startOfTrackingFound && event instanceof RaceLogStartOfTrackingEvent) {
                start = event.getLogicalTimePoint();
                startOfTrackingFound = true;
            }
            if (!endOfTrackingFound && event instanceof RaceLogEndOfTrackingEvent) {
                end = event.getLogicalTimePoint();
                endOfTrackingFound = true;
            }
            if (startOfTrackingFound && endOfTrackingFound) {
                break;
            }
        }
        final Pair<TimePoint, TimePoint> result;
        if (startOfTrackingFound || endOfTrackingFound) {
            result = new Util.Pair<TimePoint, TimePoint>(start, end);
        } else {
            result = null;
        }
        return result;
    }

}
