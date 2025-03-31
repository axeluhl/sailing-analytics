package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;

public class RaceLogORCImpliedWindSourceFinder extends BaseLogAnalyzer<RaceLog, RaceLogEvent, RaceLogEventVisitor, ImpliedWindSource> {
    public RaceLogORCImpliedWindSourceFinder(RaceLog log) {
        super(log);
    }

    @Override
    protected ImpliedWindSource performAnalysis() {
        for (final RaceLogEvent o : getLog().getUnrevokedEventsDescending()) {
            if (o instanceof RaceLogORCImpliedWindSourceEvent) {
                RaceLogORCImpliedWindSourceEvent event = (RaceLogORCImpliedWindSourceEvent) o;
                return event.getImpliedWindSource();
            }
        }
        return null;
    }

}
