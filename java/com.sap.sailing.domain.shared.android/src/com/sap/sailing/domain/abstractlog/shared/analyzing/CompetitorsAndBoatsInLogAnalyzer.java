package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;

/**
 * This class searches for {@link RaceLogRegisterCompetitorEvent}s in the given log.
 * 
 */
public class CompetitorsAndBoatsInLogAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
        extends BaseLogAnalyzer<LogT, EventT, VisitorT, Map<Competitor, Boat>> {

    public CompetitorsAndBoatsInLogAnalyzer(LogT log) {
        super(log);
    }

    @Override
    protected Map<Competitor, Boat> performAnalysis() {
        Map<Competitor, Boat> result = new HashMap<>();

        for (EventT event : getLog().getUnrevokedEvents()) {
            if (event instanceof RaceLogRegisterCompetitorEvent) {
                RaceLogRegisterCompetitorEvent competitorAndBoatEvent = (RaceLogRegisterCompetitorEvent) event;
                result.put(competitorAndBoatEvent.getCompetitor(), competitorAndBoatEvent.getBoat());
            }
        }

        return result;
    }
}