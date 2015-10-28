package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer.AnalyzerFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.base.Mark;

@Deprecated //bug2851
public class RaceLogDefinedMarkFinder extends RaceLogAnalyzer<Collection<Mark>> {
    public RaceLogDefinedMarkFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public static class Factory implements AnalyzerFactory<Collection<Mark>> {
        @Override
        public RaceLogAnalyzer<Collection<Mark>> createAnalyzer(AbstractLog<?, ?> log) {
            return new RaceLogDefinedMarkFinder((RaceLog) log);
        }
    }

    @Override
    protected Collection<Mark> performAnalysis() {
        List<Mark> result = new ArrayList<Mark>();
        for (RaceLogEvent event : getLog().getUnrevokedEvents()) {
            if (event instanceof RaceLogDefineMarkEvent) {
                RaceLogDefineMarkEvent dME = (RaceLogDefineMarkEvent) event;
                result.add(dME.getMark());
            }
        }
        return result;
    }
}
