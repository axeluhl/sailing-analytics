package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.DefineMarkEvent;

public class DefinedMarkFinder extends RaceLogAnalyzer<Iterable<Mark>> {
    public DefinedMarkFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Iterable<Mark> performAnalysis() {
        Map<Serializable, Mark> result = new HashMap<Serializable, Mark>();
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof DefineMarkEvent) {
                DefineMarkEvent dME = (DefineMarkEvent) event;
                result.put(dME.getMark().getId(), dME.getMark());
            }
        }
        return result.values();
    }

}
