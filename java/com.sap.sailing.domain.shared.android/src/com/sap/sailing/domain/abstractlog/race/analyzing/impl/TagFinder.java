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
    private final TimePoint to;
    private List<RaceLogTagEvent> tagEvents;

    public TagFinder(RaceLog raceLog) {
        this(raceLog, null, null);
    }

    public TagFinder(RaceLog raceLog, TimePoint from) {
        this(raceLog, from, null);
    }

    public TagFinder(RaceLog raceLog, TimePoint from, TimePoint to) {
        super(raceLog);
        tagEvents = new ArrayList<RaceLogTagEvent>();
        for (RaceLogEvent event : raceLog.getUnrevokedEvents()) {
            if (event instanceof RaceLogTagEvent) {
                tagEvents.add((RaceLogTagEvent) event);
            }
        }
        this.from = from;
        this.to = to == null ? MillisecondsTimePoint.now() : to;
    }

    @Override
    protected List<RaceLogTagEvent> performAnalysis() {
        List<RaceLogTagEvent> result = new ArrayList<>();
        for (RaceLogTagEvent tagEvent : tagEvents) {
            if (from == null && tagEvent.getCreatedAt().before(to)) {
                // from is not specified => load every tag until now
                result.add(tagEvent);
            } else if (from != null && tagEvent.getCreatedAt().after(from) && tagEvent.getCreatedAt().before(to)) {
                // from is specified => load only tags between from and now
                result.add(tagEvent);
            }
        }
        return result;
    }

}
