package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util.Pair;

public class RaceLogORCScratchBoatFinder extends BaseLogAnalyzer<RaceLog, RaceLogEvent, RaceLogEventVisitor, Competitor> {
    public RaceLogORCScratchBoatFinder(RaceLog log) {
        super(log);
    }

    @Override
    protected Competitor performAnalysis() {
        final Pair<Competitor, RaceLogORCScratchBoatEvent> preResult = new RaceLogORCScratchBoatAnalyzer(getLog()).analyze();
        return preResult == null ? null : preResult.getA();
    }

}
