package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.base.Mark;

public class DefinedMarkFinder extends RaceLogAnalyzer<Collection<Mark>> {
    public DefinedMarkFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Collection<Mark> performAnalysis() {
        List< Mark> result = new ArrayList<Mark>();
        for (RaceLogEvent event : getLog().getUnrevokedEvents()) {
            if (event instanceof RaceLogDefineMarkEvent) {
                RaceLogDefineMarkEvent dME = (RaceLogDefineMarkEvent) event;
                result.add(dME.getMark());
            }
        }
        return result;
    }

}
