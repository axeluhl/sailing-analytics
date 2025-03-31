package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util.Pair;

public class RaceLogORCScratchBoatAnalyzer extends BaseLogAnalyzer<RaceLog, RaceLogEvent, RaceLogEventVisitor, Pair<Competitor, RaceLogORCScratchBoatEvent>> {

    public RaceLogORCScratchBoatAnalyzer(RaceLog log) {
        super(log);
    }

    @Override
    protected Pair<Competitor, RaceLogORCScratchBoatEvent> performAnalysis() {
        for (final RaceLogEvent o : getLog().getUnrevokedEventsDescending()) {
            if (o instanceof RaceLogORCScratchBoatEvent) {
                RaceLogORCScratchBoatEvent event = (RaceLogORCScratchBoatEvent) o;
                final Competitor competitor = event.getCompetitor();
                return new Pair<>(competitor, event);
            }
        }
        return null;
    }

}
