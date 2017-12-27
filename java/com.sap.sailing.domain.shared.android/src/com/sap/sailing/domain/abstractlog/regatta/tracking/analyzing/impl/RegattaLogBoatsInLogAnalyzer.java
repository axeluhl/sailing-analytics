package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.base.Boat;

/**
 * This class searches for RegisterBoat events in the given log.
 * 
 * Boats alone in a regatta log are not enough to be used in a real race.
 * Instead a competitor and a boat together must be registered through a {@link RegisterCompetitorEvent} event. 
 *
 */
public class RegattaLogBoatsInLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Set<Boat>> {

    public RegattaLogBoatsInLogAnalyzer(LogT log) {
        super(log);
    }

    @Override
    protected Set<Boat> performAnalysis() {
        Set<Boat> result = new HashSet<Boat>();
        for (EventT event : getLog().getUnrevokedEvents()) {
            if (event instanceof RegattaLogRegisterBoatEvent) {
                result.add(((RegattaLogRegisterBoatEvent) event).getBoat());
            }
        }
        return result;
    }
}