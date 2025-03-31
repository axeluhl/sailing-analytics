package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class TrackingTimesEventFinder extends RaceLogAnalyzer<Util.Pair<RaceLogStartOfTrackingEvent, RaceLogEndOfTrackingEvent>> {

    public TrackingTimesEventFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Util.Pair<RaceLogStartOfTrackingEvent, RaceLogEndOfTrackingEvent> performAnalysis() {
        boolean startOfTrackingFound = false;
        boolean endOfTrackingFound = false;
        RaceLogStartOfTrackingEvent start = null;
        RaceLogEndOfTrackingEvent end = null;
        final RaceLog raceLog = getLog();
        for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
            if (!startOfTrackingFound && event instanceof RaceLogStartOfTrackingEvent) {
                start = (RaceLogStartOfTrackingEvent) event;
                startOfTrackingFound = true;
            }
            if (!endOfTrackingFound && event instanceof RaceLogEndOfTrackingEvent) {
                end = (RaceLogEndOfTrackingEvent) event;
                endOfTrackingFound = true;
            }
            if (startOfTrackingFound && endOfTrackingFound) {
                break;
            }
        }
        final Pair<RaceLogStartOfTrackingEvent, RaceLogEndOfTrackingEvent> result;
        if (startOfTrackingFound || endOfTrackingFound) {
            result = new Util.Pair<>(start, end);
        } else {
            result = null;
        }
        return result;
    }

}
