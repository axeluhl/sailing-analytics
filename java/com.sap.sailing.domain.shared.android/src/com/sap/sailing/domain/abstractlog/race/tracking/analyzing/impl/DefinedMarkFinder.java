package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.LogAnalyzer;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer.AnalyzerFactory;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.base.Mark;

public class DefinedMarkFinder<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Collection<Mark>> {

    public static class Factory implements AnalyzerFactory<Collection<Mark>> {
        @Override
        public LogAnalyzer<Collection<Mark>> createAnalyzer(AbstractLog<?, ?> log) {
            return new DefinedMarkFinder<>(log);
        }
    }

    public DefinedMarkFinder(LogT log) {
        super(log);
    }

    @Override
    protected Collection<Mark> performAnalysis() {
        List<Mark> result = new ArrayList<Mark>();
        for (AbstractLogEvent<?> event : getLog().getUnrevokedEvents()) {
            if (event instanceof RaceLogDefineMarkEvent) {
                RaceLogDefineMarkEvent dME = (RaceLogDefineMarkEvent) event;
                result.add(dME.getMark());
            } else if (event instanceof RegattaLogDefineMarkEvent) {
                RegattaLogDefineMarkEvent dME = (RegattaLogDefineMarkEvent) event;
                result.add(dME.getMark());
            }
        }
        return result;
    }
}
