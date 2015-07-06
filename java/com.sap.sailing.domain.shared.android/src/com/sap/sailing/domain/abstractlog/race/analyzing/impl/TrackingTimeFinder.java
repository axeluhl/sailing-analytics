package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.Date;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sse.common.Util;

public class TrackingTimeFinder extends RaceLogAnalyzer<Util.Pair<Date, Date>> {

    public TrackingTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Util.Pair<Date, Date> performAnalysis() {
        Date start = null;
        Date end = null;
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (start == null && event instanceof RaceLogStartOfTrackingEvent) {
                start = event.getLogicalTimePoint().asDate();
            }
            if (end == null && event instanceof RaceLogEndOfTrackingEvent) {
                end = event.getLogicalTimePoint().asDate();
            }
            if (start != null && end != null) {
                break;
            }
        }
        
        return new Util.Pair<Date, Date>(start, end);
    }

}
