package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.function.Function;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Util.Pair;

public class RaceLogORCScratchBoatFinder extends BaseLogAnalyzer<RaceLog, RaceLogEvent, RaceLogEventVisitor, Competitor> {
    private final Function<Serializable, Competitor> competitorsById;

    public RaceLogORCScratchBoatFinder(RaceLog log, Function<Serializable, Competitor> competitorsById) {
        super(log);
        this.competitorsById = competitorsById;
    }

    @Override
    protected Competitor performAnalysis() {
        final Pair<Competitor, RaceLogORCScratchBoatEvent> preResult = new RaceLogORCScratchBoatAnalyzer(getLog(), competitorsById).analyze();
        return preResult == null ? null : preResult.getA();
    }

}
