package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackingTimesFinder extends RaceLogAnalyzer<Util.Pair<TimePoint, TimePoint>> {

    public TrackingTimesFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Util.Pair<TimePoint, TimePoint> performAnalysis() {
        TimePoint start = null;
        TimePoint end = null;
        for (RaceLogEvent event : getLog().getUnrevokedEventsDescending()) {
            if (start == null && event instanceof RaceLogStartOfTrackingEvent) {
                start = event.getLogicalTimePoint();
            }
            if (end == null && event instanceof RaceLogEndOfTrackingEvent) {
                end = event.getLogicalTimePoint();
            }
            if (start != null && end != null) {
                break;
            }
        }
        return new Util.Pair<TimePoint, TimePoint>(start, end);
    }

}
