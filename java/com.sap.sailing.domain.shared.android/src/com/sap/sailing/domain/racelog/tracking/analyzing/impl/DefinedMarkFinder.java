package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.DefineMarkEvent;

public class DefinedMarkFinder extends RaceLogAnalyzer<Collection<Mark>> {
    public DefinedMarkFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Collection<Mark> performAnalysis() {
        List< Mark> result = new ArrayList<Mark>();
        for (RaceLogEvent event : raceLog.getUnrevokedEvents()) {
            if (event instanceof DefineMarkEvent) {
                DefineMarkEvent dME = (DefineMarkEvent) event;
                result.add(dME.getMark());
            }
        }
        return result;
    }

}
