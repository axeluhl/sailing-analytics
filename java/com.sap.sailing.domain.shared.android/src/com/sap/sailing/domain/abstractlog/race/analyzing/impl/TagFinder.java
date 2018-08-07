package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TagFinder extends RaceLogAnalyzer<List<RaceLogTagEvent>> {

    private final TimePoint from;

    public TagFinder(RaceLog raceLog, TimePoint from) {
        super(raceLog);
        this.from = from;
    }

    @Override
    protected List<RaceLogTagEvent> performAnalysis() {
        List<RaceLogTagEvent> result = new ArrayList<>();
        TimePoint now = MillisecondsTimePoint.now();
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogTagEvent) {
                RaceLogTagEvent tagEvent = (RaceLogTagEvent) event;
                if (from == null && tagEvent.getCreatedAt().before(now)) {
                    // from is not specified => load every tag until now
                    result.add(tagEvent);
                } else if (from != null && tagEvent.getCreatedAt().after(from) && tagEvent.getCreatedAt().before(now)) {
                    // from is specified => load only tags between from and now
                    result.add(tagEvent);
                }
            }
        }
        return result;
    }

}
