package com.sap.sailing.domain.racelog.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.SuppressedMarkPassingsEvent;
import com.sap.sse.common.Util.Pair;

public class SuppressedMarkPassingsFinder extends RaceLogAnalyzer<Set<Pair<Competitor, Integer>>> {

    public SuppressedMarkPassingsFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Set<Pair<Competitor, Integer>> performAnalysis() {
        Set<Pair<Competitor, Integer>> result = new HashSet<Pair<Competitor, Integer>>();
        for (RaceLogEvent event : getRaceLog().getUnrevokedEvents()){
            if (event instanceof SuppressedMarkPassingsEvent){
               result.add(new Pair<Competitor, Integer>(event.getInvolvedBoats().get(0),((SuppressedMarkPassingsEvent) event).getIndexOfFirstSuppressedWaypoint()));
            }
        }
        return result;
    }

}
