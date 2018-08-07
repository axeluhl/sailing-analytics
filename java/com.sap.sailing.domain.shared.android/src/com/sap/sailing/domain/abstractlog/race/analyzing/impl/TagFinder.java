package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sse.common.TimePoint;

public class TagFinder extends RaceLogAnalyzer<List<RaceLogTagEvent>> {

    private final TimePoint from;
    private final TimePoint to;

    public TagFinder(RaceLog raceLog, TimePoint from, TimePoint to) {
        super(raceLog);
        this.from = from;
        this.to = to;
    }

    @Override
    protected List<RaceLogTagEvent> performAnalysis() {
        List<RaceLogTagEvent> result = new ArrayList<>();
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogTagEvent) {
                RaceLogTagEvent tagEvent = (RaceLogTagEvent) event;
                if (from == null && tagEvent.getCreatedAt().before(to)) {
                    // from is not specified => load every tag until to
                    result.add(tagEvent);
                } else if (from != null && tagEvent.getCreatedAt().after(from) && tagEvent.getCreatedAt().before(to)) {
                    // from is specified => load only tags between from and to
                    result.add(tagEvent);
                }
            }
        }
        return result;
    }

}
